package io.antarescircuit.antares.ai

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A read-only snapshot of the circuit that is currently open in the editor.
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
	val connections: List<AiCircuitConnection> = emptyList()
) {

	companion object {

		/** Upper bound of components described to the model, keeping the prompt size bounded.*/
		const val MAX_COMPONENTS = 200

		private val json = Json { prettyPrint = false; encodeDefaults = true }

		/** Builds a snapshot of [graphView]. Must be called on the UI thread. */
		fun of(graphView: GraphView): AiCircuitContext {
			val verticeViews = graphView.getVerticeViews()
			val included = verticeViews.take(MAX_COMPONENTS)
			val includedIds = included.map { it.id }.toSet()

			return AiCircuitContext(
				circuitName = graphView.graph?.name?.value ?: graphView.name.value,
				circuitId = graphView.graph?.uuid?.id ?: "",
				omittedComponents = verticeViews.size - included.size,
				components = included.map { describe(it) },
				connections = graphView.getEdgeViews().mapNotNull { describe(it, includedIds) }
			)
		}

		private fun describe(verticeView: VerticeView<*>): AiCircuitComponent {
			val vertice = verticeView.vertice
			val inputs = vertice.getInputs()
			return AiCircuitComponent(
				ref = AiRef.Existing(verticeView.id).toString(),
				type = describeType(verticeView),
				name = vertice.name,
				inputCount = inputs.size,
				outputCount = vertice.getOutputs().size,
				connectedInputs = inputs.mapIndexedNotNull { index, port -> if (port.isConnected) index + 1 else null },
				x = verticeView.location.xInt,
				y = verticeView.location.yInt
			)
		}

		/**
		 * Uses the translated component type, and marks circuit ports explicitly, because their
		 * type name alone doesn't reveal the signal direction.
		 */
		private fun describeType(verticeView: VerticeView<*>): String {
			val model = verticeView.vertice
			if (model is DigitalCircuitInOut) {
				return when (model.portType) {
					PortType.INPUT -> AiComponentType.Input.id
					PortType.OUTPUT -> AiComponentType.Output.id
					else -> "inout"
				}
			}
			return model.type
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
			val ports = if (output) verticeView.vertice.getOutputs() else verticeView.vertice.getInputs()
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
	val x: Int = 0,
	val y: Int = 0
)

@Serializable
data class AiCircuitConnection(
	val from: String,
	val fromPort: Int,
	val to: String,
	val toPort: Int
)
