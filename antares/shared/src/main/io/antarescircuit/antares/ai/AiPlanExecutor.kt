package io.antarescircuit.antares.ai

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.gate.UnaryLogicGate
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.model.net.Constant
import io.antarescircuit.antares.model.output.LED
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.net.ConstantView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.editor.AddCommand
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.connect.ConnectDestinationCommand
import io.antarescircuit.jabbah.graph.view.connect.ConnectOriginCommand
import io.antarescircuit.jabbah.graph.view.connect.NewEdgeViewAtSplitCloneProvider
import io.antarescircuit.jabbah.graph.view.connect.SplitEdgeViewCommand
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewEndpointType
import kotlin.math.roundToInt

/**
 * Applies an [AiValidatedPlan] to the [GraphView] of an [Editor].
 *
 * The whole plan is wrapped in a single [io.antarescircuit.jabbah.edit.CommandManager] transaction,
 * so that the user can revert everything the assistant did with a single "Undo". If any operation
 * fails, the transaction is rolled back and the circuit is left untouched.
 *
 * Must be called on the UI thread.
 */
class AiPlanExecutor(
	private val customAppService: DrawingAppService? = null
) {

	/** Resolved on every use, because modules replace the service during bootstrap.*/
	private val appService: DrawingAppService get() = customAppService ?: EditModule.drawingAppService

	companion object {

		private val LOG by logger(AiPlanExecutor::class)

		/** The translation key describing the transaction in the undo/redo menu.*/
		const val COMMAND_KEY = "antares.ai.command.apply"

		/** Horizontal distance between auto-placed columns.*/
		private const val AUTO_COLUMN_WIDTH = Look.SCALE * 12

		/** Vertical distance between auto-placed rows.*/
		private const val AUTO_ROW_HEIGHT = Look.SCALE * 8

		/** Number of auto-placed components per column.*/
		private const val AUTO_ROWS_PER_COLUMN = 8

		private fun snapToGrid(value: Int): Int =
			(value.toDouble() / Look.GRID).roundToInt() * Look.GRID
	}

	data class Result(
		val addedComponents: Int,
		val connections: Int,
		val deletedComponents: Int
	)

	/**
	 * Applies [plan] to the circuit currently shown by [editor].
	 * @throws AiPlanExecutionException if an operation could not be applied. The circuit is unchanged in that case.
	 */
	fun apply(plan: AiValidatedPlan, editor: Editor): Result {
		val drawingView = editor.view
		val graphView = editor.drawing as? GraphView
			?: throw AiPlanExecutionException("The active view does not show a circuit.")

		val created = mutableMapOf<String, Int>()
		var added = 0
		var connections = 0
		var deleted = 0
		var autoPlaced = 0

		editor.commandManager.beginTransaction(COMMAND_KEY, drawingView)
		try {
			plan.operations.forEach { operation ->
				when (operation) {
					is AiOperation.ClearCircuit -> deleted += clear(graphView, drawingView)

					is AiOperation.DeleteComponent -> deleted += delete(operation, graphView, drawingView)

					is AiOperation.AddComponent -> {
						val location = locationOf(operation, autoPlaced)
						if (operation.x == null || operation.y == null) {
							autoPlaced++
						}
						created[operation.ref.id] = add(operation, location, editor, drawingView)
						added++
					}

					is AiOperation.Connect -> {
						connect(operation, created, editor, graphView)
						connections++
					}
				}
			}
			editor.commandManager.commitTransaction()
		} catch (e: Throwable) {
			LOG.error("Rolling back AI plan: ${e.message}")
			try {
				editor.commandManager.rollbackTransaction()
			} catch (rollbackFailure: Throwable) {
				LOG.error("Rollback of AI plan failed: ${rollbackFailure.message}")
			}
			throw if (e is AiPlanExecutionException) e else AiPlanExecutionException(e.message ?: e::class.simpleName ?: "unknown error", e)
		}

		selectCreated(created.values, drawingView)

		LOG.userTrail("Applied AI plan: $added component(s), $connections connection(s), $deleted deletion(s)")

		return Result(added, connections, deleted)
	}

	private fun clear(graphView: GraphView, drawingView: DrawingView<Component, Drawing<Component>>): Int {
		val components = graphView.drawables.filter { it.deletable }.toList()
		if (components.isEmpty()) {
			return 0
		}
		appService.delete(components, drawingView)
		return components.size
	}

	private fun delete(
		operation: AiOperation.DeleteComponent,
		graphView: GraphView,
		drawingView: DrawingView<Component, Drawing<Component>>
	): Int {
		val component = graphView.getWithId(operation.target.componentId)
			?: throw AiPlanExecutionException("Component ${operation.target} is no longer part of the circuit.")
		if (!component.deletable) {
			throw AiPlanExecutionException("Component ${operation.target} cannot be deleted.")
		}
		appService.delete(listOf(component), drawingView)
		return 1
	}

	/** Adds a component and returns the ID it received in the drawing. */
	private fun add(
		operation: AiOperation.AddComponent,
		location: Point2D,
		editor: Editor,
		drawingView: DrawingView<Component, Drawing<Component>>
	): Int {
		val verticeView = createVerticeView(operation)
		verticeView.location = location

		// Deliberately not going through DrawingAppService.add, which would change the selection
		// for every single component of a potentially large plan.
		val command = AddCommand(drawingView, verticeView, appService)
		editor.commandManager.execute(command)
		return command.addedComponentId
	}

	private fun createVerticeView(operation: AiOperation.AddComponent): VerticeView<*> = when (operation.type) {
		AiComponentType.Input -> DigitalCircuitInOutView(
			model = DigitalCircuitInOutImpl(name = operation.name, portType = PortType.INPUT))

		AiComponentType.Output -> DigitalCircuitInOutView(
			model = DigitalCircuitInOutImpl(name = operation.name, portType = PortType.OUTPUT))

		AiComponentType.Switch -> SwitchView(model = Switch()).named(operation.name)

		AiComponentType.Led -> LEDView(model = LED()).named(operation.name)

		AiComponentType.Constant -> ConstantView(model = Constant(LongValueImpl(operation.value))).named(operation.name)

		AiComponentType.Not -> LogicGateView(gate = UnaryLogicGate.notGate()).named(operation.name)

		AiComponentType.Buffer -> LogicGateView(gate = UnaryLogicGate.bufferGate()).named(operation.name)

		AiComponentType.And -> gate(NonUnaryLogicGateType.And, operation)
		AiComponentType.Or -> gate(NonUnaryLogicGateType.Or, operation)
		AiComponentType.Nand -> gate(NonUnaryLogicGateType.Nand, operation)
		AiComponentType.Nor -> gate(NonUnaryLogicGateType.Nor, operation)
		AiComponentType.Xor -> gate(NonUnaryLogicGateType.Xor, operation)
		AiComponentType.Xnor -> gate(NonUnaryLogicGateType.Xnor, operation)
	}

	private fun gate(type: NonUnaryLogicGateType, operation: AiOperation.AddComponent): LogicGateView =
		LogicGateView(gate = NonUnaryLogicGate(type, PortCount.of(operation.inputCount))).named(operation.name)

	private fun <T : VerticeView<*>> T.named(name: String?): T = also {
		if (!name.isNullOrBlank()) {
			it.vertice.name = name
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun connect(
		operation: AiOperation.Connect,
		created: Map<String, Int>,
		editor: Editor,
		graphView: GraphView
	) {
		val origin = resolve(operation.from, created, graphView)
		val destination = resolve(operation.to, created, graphView)

		val originPort = origin.vertice.getOutputs().getOrNull(operation.fromPort - 1)
			?: throw AiPlanExecutionException("'${operation.from}' has no output ${operation.fromPort}.")
		val destinationPort = destination.vertice.getInputs().getOrNull(operation.toPort - 1)
			?: throw AiPlanExecutionException("'${operation.to}' has no input ${operation.toPort}.")

		val existingEdge = graphView.getEdgeView(originPort)
		if (existingEdge != null) {
			// Antares represents fan-out by branching from the existing wire, not by attaching a
			// second net to the source port. SplitEdgeViewCommand inserts the required junction.
			val splitLocation = existingEdge.polyline.getCenterOfSegment(0)
			val branch = GraphViewModule.getEdgeViewFactory()
				.createEdgeView<DigitalSignal>(graphView)
				.addSegmentPoint(splitLocation)
				.addSegmentPoint(destination.getPortConnectionPoint(destinationPort))
			editor.commandManager.execute(SplitEdgeViewCommand(
				editor = editor,
				connectService = GraphViewModule.graphViewConnectService,
				splitEdgeViewId = existingEdge.id,
				segmentIndex = 0,
				splitLocation = splitLocation,
				newEdgeViewProvider = NewEdgeViewAtSplitCloneProvider(branch),
				newEdgeViewEndpointType = EdgeViewEndpointType.ORIGIN,
				targetConnectableViewId = destination.id,
				targetPortId = destinationPort.portId))
			return
		}

		val edgeView = GraphViewModule.getEdgeViewFactory()
			.createEdgeView<DigitalSignal>(graphView)
			.addSegmentPoint(origin.getPortConnectionPoint(originPort))
			.addSegmentPoint(destination.getPortConnectionPoint(destinationPort))

		val addedEdgeView = appService.add(edgeView, editor.view) as EdgeView<DigitalSignal>

		editor.commandManager.execute(ConnectOriginCommand(
			editor,
			GraphViewModule.graphViewConnectService,
			addedEdgeView.id,
			origin.id,
			originPort.portId))

		editor.commandManager.execute(ConnectDestinationCommand(
			editor,
			GraphViewModule.graphViewConnectService,
			addedEdgeView.id,
			destination.id,
			destinationPort.portId))
	}

	private fun resolve(ref: AiRef, created: Map<String, Int>, graphView: GraphView): VerticeView<*> {
		val componentId = when (ref) {
			is AiRef.New -> created[ref.id]
				?: throw AiPlanExecutionException("'${ref.id}' was not created by this plan.")
			is AiRef.Existing -> ref.componentId
		}
		return graphView.getWithId(componentId) as? VerticeView<*>
			?: throw AiPlanExecutionException("'$ref' is not a connectable component of the circuit.")
	}

	private fun locationOf(operation: AiOperation.AddComponent, autoPlaced: Int): Point2D {
		if (operation.x != null && operation.y != null) {
			return Point2D(snapToGrid(operation.x), snapToGrid(operation.y))
		}
		return Point2D(
			(autoPlaced / AUTO_ROWS_PER_COLUMN) * AUTO_COLUMN_WIDTH,
			(autoPlaced % AUTO_ROWS_PER_COLUMN) * AUTO_ROW_HEIGHT)
	}

	private fun selectCreated(componentIds: Collection<Int>, drawingView: DrawingView<Component, Drawing<Component>>) {
		val components = componentIds.mapNotNull { drawingView.drawing.getWithId(it) }
		drawingView.selectionManager.deselectAll()
		if (components.isNotEmpty()) {
			drawingView.selectionManager.select(components)
		}
	}
}

/** Thrown when a validated plan could not be applied. The circuit is rolled back to its previous state. */
class AiPlanExecutionException(message: String, cause: Throwable? = null) : Exception(message, cause)
