package io.antarescircuit.antares.ai

import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.gate.TriStateBufferGate
import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.input.Clock
import io.antarescircuit.antares.model.net.Constant
import io.antarescircuit.antares.model.net.Concentrator
import io.antarescircuit.antares.model.net.Splitter
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A read-only snapshot of the circuit [GraphView] that is currently open in the editor.
 *
 * The snapshot is the only information about the user's circuit that is sent to the AI, and it is
 * at the same time the base against which [AiPlanValidator] checks the references of a returned plan.
 * Taking a snapshot decouples validation from the live editor and makes it unit-testable.
 */
@Serializable
data class AiCircuitContext(
	val circuitName: String,
	/** Identifies the circuit this snapshot was taken from, so that a plan can be bound to it.*/
	val circuitId: String = "",
	/** The number of components that were omitted because [MAX_COMPONENTS] was exceeded.*/
	val omittedComponents: Int = 0,
	val components: List<AiCircuitComponent> = emptyList(),
	val connections: List<AiCircuitConnection> = emptyList(),
	val availableSubcircuits: List<AiAvailableSubcircuit> = emptyList(),
	val omittedSubcircuits: Int = 0
) {

	companion object {

		/** Upper bound of components described to the model, keeping the prompt size bounded.*/
		const val MAX_COMPONENTS = 200
		const val MAX_SUBCIRCUITS = 200

		private val json = Json { prettyPrint = false; encodeDefaults = true }

		/** Builds a snapshot of [graphView]. Must be called on the UI thread. */
		fun of(graphView: GraphView, library: Library? = LibraryModule.libraryHolder.l): AiCircuitContext {
			val verticeViews = graphView.getVerticeViews()
			val included = verticeViews.take(MAX_COMPONENTS)
			val includedIds = included.map { it.id }.toSet()
			val subcircuits = library?.let { availableSubcircuits(graphView, it) }.orEmpty()

			return AiCircuitContext(
				circuitName = graphView.graph?.name?.value ?: graphView.name.value,
				circuitId = graphView.graph?.uuid?.id ?: "",
				omittedComponents = verticeViews.size - included.size,
				components = included.map { describe(it) },
				connections = graphView.getEdgeViews().mapNotNull { describe(it, includedIds) },
				availableSubcircuits = subcircuits.take(MAX_SUBCIRCUITS),
				omittedSubcircuits = (subcircuits.size - MAX_SUBCIRCUITS).coerceAtLeast(0)
			)
		}

		private fun describe(verticeView: VerticeView<*>): AiCircuitComponent {
			val vertice = verticeView.vertice
			val inputs = if (verticeView is SubGraphVerticeView<*>) {
				vertice.getInputs().filter { it.portType == PortType.INPUT }
			} else {
				vertice.getInputs()
			}
			val outputs = if (verticeView is SubGraphVerticeView<*>) {
				vertice.getOutputs().filter { it.portType == PortType.OUTPUT }
			} else {
				vertice.getOutputs()
			}
			return AiCircuitComponent(
				ref = AiRef.Existing(verticeView.id).toString(),
				type = describeType(verticeView),
				name = vertice.name,
				inputCount = inputs.size,
				outputCount = outputs.size,
				connectedInputs = inputs.mapIndexedNotNull { index, port -> if (port.isConnected) index + 1 else null },
				metaGraphUuid = (verticeView as? SubGraphVerticeView<*>)?.subGraphVertice?.graphUUID?.id,
				inputPorts = if (verticeView is SubGraphVerticeView<*>) describePorts(verticeView, output = false) else emptyList(),
				outputPorts = if (verticeView is SubGraphVerticeView<*>) describePorts(verticeView, output = true) else emptyList(),
				bitWidth = when (vertice) {
					is DigitalCircuitInOut -> vertice.bitWidth.width
					is AbstractLogicGate -> vertice.bitWidth.width
					is Constant -> vertice.bitWidth.width
					is Splitter -> vertice.bitWidth.width
					is Concentrator -> vertice.bitWidth.width
					is TriStateBufferGate -> vertice.bitWidth.width
					else -> null
				},
				branchCount = when (vertice) {
					is Splitter -> vertice.branchCount.count
					is Concentrator -> vertice.branchCount.count
					else -> null
				},
				enableLogic = (vertice as? TriStateBufferGate)?.enableLogic?.customName,
				periodOrFrequency = (vertice as? Clock)?.periodOrFrequency?.toString(),
				x = verticeView.location.xInt,
				y = verticeView.location.yInt
			)
		}

		/**
		 * Uses the translated component type, and marks circuit ports explicitly, because their
		 * type name alone doesn't reveal the signal direction.
		 */
		private fun describeType(verticeView: VerticeView<*>): String {
			if (verticeView is SubGraphVerticeView<*>) {
				return AiComponentType.Subcircuit.id
			}
			val model = verticeView.vertice
			if (model is DigitalCircuitInOut) {
				return when (model.portType) {
					PortType.INPUT -> AiComponentType.Input.id
					PortType.OUTPUT -> AiComponentType.Output.id
					else -> "inout"
				}
			}
			if (model is Splitter) {
				return AiComponentType.Splitter.id
			}
			if (model is Concentrator) {
				return AiComponentType.Concentrator.id
			}
			if (model is TriStateBufferGate) {
				return AiComponentType.TriStateBuffer.id
			}
			if (model is Clock) {
				return AiComponentType.Clock.id
			}
			return model.type
		}

		private fun availableSubcircuits(graphView: GraphView, library: Library): List<AiAvailableSubcircuit> {
			val targetGraph = graphView.graph ?: return emptyList()
			val repository = LibraryModule.libraryHolder
			val libraries = if (library.importedLibraryIds.isEmpty()) listOf(library) else library.expandedImports.libraries
			return libraries
				.filterNot { it.isBrokenImport }
				.flatMap { sourceLibrary ->
					sourceLibrary.allLocalItems { it is ContainerLibraryElement }
						.filterIsInstance<ContainerLibraryElement>()
						.mapNotNull { element ->
							try {
								if (targetGraph.type.checkImport(element) != null
									|| repository.graphContainsRecursively(element.uuid, targetGraph.uuid)
								) {
									return@mapNotNull null
								}
								val metaGraph = sourceLibrary.libraryService.getMetaGraph(sourceLibrary, element)
								val view = element.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<*>
								try {
									AiAvailableSubcircuit(
										uuid = element.uuid.id,
										name = metaGraph.name,
										description = metaGraph.graph.model?.description?.value?.ifBlank { null },
										libraryName = sourceLibrary.name.value,
										inputPorts = describePorts(view, output = false),
										outputPorts = describePorts(view, output = true),
										bidirectionalPorts = view.vertice.getPorts().filter { it.portType == PortType.INOUT }.mapIndexed { index, port ->
											AiSubcircuitPort(index + 1, port.name, (port as? DigitalPort)?.bitWidth?.width)
										}
									)
								} finally {
									view.dispose()
								}
							} catch (_: Exception) {
								null
							}
						}
				}
				.sortedWith(compareBy({ it.libraryName }, { it.name }, { it.uuid }))
		}

		private fun describePorts(view: VerticeView<*>, output: Boolean): List<AiSubcircuitPort> {
			val portType = if (output) PortType.OUTPUT else PortType.INPUT
			val ports = view.vertice.getPorts().filter { it.portType == portType }
			return ports.mapIndexed { index, port ->
				AiSubcircuitPort(index + 1, port.name, (port as? DigitalPort)?.bitWidth?.width)
			}
		}

		private fun describe(edgeView: EdgeView<*>, includedIds: Set<Int>): AiCircuitConnection? {
			val originView = edgeView.origin?.connectableView as? VerticeView<*> ?: return null
			val destinationView = edgeView.destination?.connectableView as? VerticeView<*> ?: return null
			if (!includedIds.contains(originView.id) || !includedIds.contains(destinationView.id)) {
				// Skip connections routed over nodes or over components that were omitted
				return null
			}
			val fromPort = indexOfPort(edgeView.origin?.port?.portId, originView, output = true) ?: return null
			val toPort = indexOfPort(edgeView.destination?.port?.portId, destinationView, output = false) ?: return null
			return AiCircuitConnection(
				from = AiRef.Existing(originView.id).toString(),
				fromPort = fromPort,
				to = AiRef.Existing(destinationView.id).toString(),
				toPort = toPort
			)
		}

		/** Translates a port ID into the 1-based input or output index used by the operation contract. */
		private fun indexOfPort(portId: Int?, verticeView: VerticeView<*>, output: Boolean): Int? {
			if (portId == null) {
				return null
			}
			val ports = if (output) {
				verticeView.vertice.getOutputs().filter { it.portType == PortType.OUTPUT }
			} else {
				verticeView.vertice.getInputs().filter { it.portType == PortType.INPUT }
			}
			val index = ports.indexOfFirst { it.portId == portId }
			return if (index < 0) null else index + 1
		}
	}

	val isEmpty: Boolean get() = components.isEmpty()

	/** Returns the described component with the given editor component ID, if any. */
	fun component(componentId: Int): AiCircuitComponent? =
		components.find { it.ref == AiRef.Existing(componentId).toString() }

	/** The names of all circuit ports, which must stay unique within a circuit. */
	fun portNames(): Set<String> = components
		.filter { it.type == AiComponentType.Input.id || it.type == AiComponentType.Output.id || it.type == "inout" }
		.mapNotNull { it.name }
		.toSet()

	fun toPromptJson(): String = json.encodeToString(this)
}

@Serializable
data class AiCircuitComponent(
	val ref: String,
	val type: String,
	val name: String? = null,
	val inputCount: Int = 0,
	val outputCount: Int = 0,
	/** The 1-based indices of the inputs that already have a wire attached.*/
	val connectedInputs: List<Int> = emptyList(),
	val metaGraphUuid: String? = null,
	val inputPorts: List<AiSubcircuitPort> = emptyList(),
	val outputPorts: List<AiSubcircuitPort> = emptyList(),
	/** Signal width for circuit ports, constants, splitters, and logic gates. */
	val bitWidth: Int? = null,
	/** Number of equal-width narrow buses, present for splitters and concentrators. */
	val branchCount: Int? = null,
	/** Enable polarity, present only for tri-state buffers. */
	val enableLogic: String? = null,
	/** Period or frequency including its SI unit, present only for clocks. */
	val periodOrFrequency: String? = null,
	val x: Int = 0,
	val y: Int = 0
)

@Serializable
data class AiAvailableSubcircuit(
	val uuid: String,
	val name: String,
	val description: String? = null,
	val libraryName: String,
	val inputPorts: List<AiSubcircuitPort> = emptyList(),
	val outputPorts: List<AiSubcircuitPort> = emptyList(),
	/** Listed for understanding only; the AI connection operation does not support bidirectional ports. */
	val bidirectionalPorts: List<AiSubcircuitPort> = emptyList()
)

@Serializable
data class AiSubcircuitPort(
	val index: Int,
	val name: String? = null,
	val bitWidth: Int? = null
)

@Serializable
data class AiCircuitConnection(
	val from: String,
	val fromPort: Int,
	val to: String,
	val toPort: Int
)
