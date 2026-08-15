package io.antarescircuit.antares.ai

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.AbstractLogicGate
import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.gate.TriStateBufferGate
import io.antarescircuit.antares.model.gate.UnaryLogicGate
import io.antarescircuit.antares.model.gate.UnaryLogicGateType
import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.model.input.Clock
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.Constant
import io.antarescircuit.antares.model.net.Concentrator
import io.antarescircuit.antares.model.net.Splitter
import io.antarescircuit.antares.model.output.LED
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalSource
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.input.ClockView
import io.antarescircuit.antares.view.net.ConstantView
import io.antarescircuit.antares.view.net.ConcentratorView
import io.antarescircuit.antares.view.net.SplitterView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.app.DrawingAppService
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.editor.AddCommand
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
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

		/** Distance kept between an existing circuit and newly created components.*/
		private const val EXISTING_CIRCUIT_MARGIN = Look.SCALE * 12

		private fun snapToGrid(value: Int): Int =
			(value.toDouble() / Look.GRID).roundToInt() * Look.GRID
	}

	data class Result(
		val addedComponents: Int,
		val connections: Int,
		val deletedComponents: Int,
		val changedBitWidths: Int
	)

	private data class EdgeSplit(val segmentIndex: Int, val location: Point2D)
	private data class ColumnBounds(val x: Int, val minX: Double, val maxX: Double) {
		val width: Double get() = maxX - minX
	}

	private inner class FanOutSplitAllocator {
		private var nextSlot = 1

		fun allocate(edgeView: EdgeView<*>, count: Int): ArrayDeque<EdgeSplit> {
			val segmentLengths = segmentLengths(edgeView)
			return (count downTo 1).mapTo(ArrayDeque()) {
				val distance = edgeView.polyline.length * vanDerCorput(nextSlot++)
				splitLocationAt(edgeView, segmentLengths, distance)
			}
		}

		private fun vanDerCorput(index: Int): Double {
			var value = index
			var denominator = 1.0
			var result = 0.0
			while (value > 0) {
				denominator *= 2.0
				result += (value % 2) / denominator
				value /= 2
			}
			return result
		}
	}

	private class SetBitWidthCommand(
		editor: Editor,
		private val target: io.antarescircuit.jabbah.graph.model.Vertice,
		private val bitWidth: BitWidth
	) : AbstractCommand(COMMAND_KEY, editor), Undoable {

		private val oldBitWidth = bitWidthOf(target)

		override fun execute() {
			setBitWidth(target, bitWidth)
		}

		override fun undo() {
			setBitWidth(target, oldBitWidth)
		}

		companion object {
			private fun bitWidthOf(target: io.antarescircuit.jabbah.graph.model.Vertice): BitWidth = when (target) {
				is DigitalSignalSource -> target.bitWidth
				is AbstractLogicGate -> target.bitWidth
				is TriStateBufferGate -> target.bitWidth
				else -> throw IllegalArgumentException("Component does not support changing its bit width.")
			}

			private fun setBitWidth(target: io.antarescircuit.jabbah.graph.model.Vertice, bitWidth: BitWidth) {
				when (target) {
					is DigitalSignalSource -> target.bitWidth = bitWidth
					is AbstractLogicGate -> target.bitWidth = bitWidth
					is TriStateBufferGate -> target.bitWidth = bitWidth
					else -> throw IllegalArgumentException("Component does not support changing its bit width.")
				}
			}
		}
	}

	/**
	 * Applies [plan] to the circuit currently shown by [editor].
	 * @throws AiPlanExecutionException if an operation could not be applied. The circuit is unchanged in that case.
	 */
	fun apply(plan: AiValidatedPlan, editor: Editor): Result {
		val drawingView = editor.view
		val graphView = editor.drawing as? GraphView
			?: throw AiPlanExecutionException("The active view does not show a circuit.")
		val laidOutPlan = alignSourcePorts(equalizeColumnGaps(AiPlanLayouter.layout(plan)))

		val created = mutableMapOf<String, Int>()
		val fanOutSplits = mutableMapOf<Pair<AiRef, Int>, ArrayDeque<EdgeSplit>>()
		val fanOutSplitAllocator = FanOutSplitAllocator()
		var added = 0
		var connections = 0
		var deleted = 0
		var changedBitWidths = 0
		var placementOffset = placementOffset(laidOutPlan, graphView)

		editor.commandManager.beginTransaction(COMMAND_KEY, drawingView)
		try {
			laidOutPlan.operations.forEachIndexed { index, operation ->
				when (operation) {
					is AiOperation.ClearCircuit -> {
						deleted += clear(graphView, drawingView)
						created.clear()
						fanOutSplits.clear()
						placementOffset = Point2D.ZERO
					}

					is AiOperation.DeleteComponent -> deleted += delete(operation, graphView, drawingView)

					is AiOperation.AddComponent -> {
						val location = locationOf(operation, placementOffset)
						created[operation.ref.id] = add(operation, location, editor, drawingView)
						added++
					}

					is AiOperation.Connect -> {
						connect(
							operation,
							created,
							editor,
							graphView,
							fanOutSplits,
							fanOutSplitAllocator,
							remainingConnections(laidOutPlan.operations, index, operation))
						connections++
					}

					is AiOperation.SetBitWidth -> {
						setBitWidth(operation, created, editor, graphView)
						changedBitWidths++
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

		return Result(added, connections, deleted, changedBitWidths)
	}

	private fun setBitWidth(
		operation: AiOperation.SetBitWidth,
		created: Map<String, Int>,
		editor: Editor,
		graphView: GraphView
	) {
		val target = resolve(operation.target, created, graphView)
		val model = target.vertice
		if (model !is DigitalSignalSource && model !is AbstractLogicGate && model !is TriStateBufferGate) {
			throw AiPlanExecutionException("'${operation.target}' does not support changing its bit width.")
		}
		editor.commandManager.execute(SetBitWidthCommand(
			editor = editor,
			target = model,
			bitWidth = BitWidth.of(operation.bitWidth)))
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
			model = DigitalCircuitInOutImpl(name = operation.name, portType = PortType.INPUT, bitWidth = operation.bitWidth()))

		AiComponentType.Output -> DigitalCircuitInOutView(
			model = DigitalCircuitInOutImpl(name = operation.name, portType = PortType.OUTPUT, bitWidth = operation.bitWidth()))

		AiComponentType.Switch -> SwitchView(model = Switch()).named(operation.name)

		AiComponentType.Clock -> ClockView(model = Clock(operation.name).also { clock ->
			operation.periodOrFrequency?.let { clock.periodOrFrequency = it }
		})

		AiComponentType.Led -> LEDView(model = LED()).named(operation.name)

		AiComponentType.Constant -> ConstantView(model = Constant(LongValueImpl(operation.value)).also {
			it.bitWidth = operation.bitWidth()
		}).named(operation.name)

		AiComponentType.Splitter -> SplitterView(model = Splitter(
			bitWidth = operation.bitWidth(),
			branchCount = BranchCount.withCount(operation.branchCount
				?: throw AiPlanExecutionException("Splitter '${operation.ref}' has no branch count.")))).also {
			it.handedness = Handedness.LEFT
		}.named(operation.name)

		AiComponentType.Concentrator -> ConcentratorView(
			model = Concentrator(
				bitWidth = operation.bitWidth(),
				branchCount = BranchCount.withCount(operation.branchCount
					?: throw AiPlanExecutionException("Concentrator '${operation.ref}' has no branch count."))),
			handedness = Handedness.LEFT
		).named(operation.name)

		AiComponentType.TriStateBuffer -> TriStateBufferGateView(model = TriStateBufferGate(
			bitWidth = operation.bitWidth(),
			enableLogic = operation.enableLogic
				?: throw AiPlanExecutionException("Tri-state buffer '${operation.ref}' has no enable logic."))).named(operation.name)

		AiComponentType.Subcircuit -> createSubcircuit(operation)

		AiComponentType.Not -> LogicGateView(gate = UnaryLogicGate.notGate(operation.bitWidth())).named(operation.name)

		AiComponentType.Buffer -> LogicGateView(gate = UnaryLogicGate(UnaryLogicGateType.Buffer, operation.bitWidth())).named(operation.name)

		AiComponentType.And -> gate(NonUnaryLogicGateType.And, operation)
		AiComponentType.Or -> gate(NonUnaryLogicGateType.Or, operation)
		AiComponentType.Nand -> gate(NonUnaryLogicGateType.Nand, operation)
		AiComponentType.Nor -> gate(NonUnaryLogicGateType.Nor, operation)
		AiComponentType.Xor -> gate(NonUnaryLogicGateType.Xor, operation)
		AiComponentType.Xnor -> gate(NonUnaryLogicGateType.Xnor, operation)
	}

	private fun createSubcircuit(operation: AiOperation.AddComponent): SubGraphVerticeView<*> {
		val rawUuid = operation.metaGraphUuid
			?: throw AiPlanExecutionException("Subcircuit '${operation.ref}' has no MetaGraph UUID.")
		val uuid = try {
			UUID(rawUuid)
		} catch (e: Exception) {
			throw AiPlanExecutionException("Subcircuit '${operation.ref}' has an invalid MetaGraph UUID '$rawUuid'.", e)
		}
		val element = LibraryModule.libraryHolder.getContainerLibraryElement(uuid)
			?: throw AiPlanExecutionException("MetaGraph '$rawUuid' is no longer available.")
		return try {
			element.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<*>
		} catch (e: Exception) {
			throw AiPlanExecutionException("Could not create subcircuit '${element.name.value}'.", e)
		}
	}

	private fun gate(type: NonUnaryLogicGateType, operation: AiOperation.AddComponent): LogicGateView =
		LogicGateView(gate = NonUnaryLogicGate(type, PortCount.of(operation.inputCount), operation.bitWidth())).named(operation.name)

	private fun AiOperation.AddComponent.bitWidth(): BitWidth = BitWidth.of(bitWidth)

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
		graphView: GraphView,
		fanOutSplits: MutableMap<Pair<AiRef, Int>, ArrayDeque<EdgeSplit>>,
		fanOutSplitAllocator: FanOutSplitAllocator,
		remainingConnections: Int
	) {
		val origin = resolve(operation.from, created, graphView)
		val destination = resolve(operation.to, created, graphView)

		val originPort = origin.vertice.getOutputs().filter { it.portType == PortType.OUTPUT }.getOrNull(operation.fromPort - 1)
			?: throw AiPlanExecutionException("'${operation.from}' has no output ${operation.fromPort}.")
		val destinationPort = destination.vertice.getInputs().filter { it.portType == PortType.INPUT }.getOrNull(operation.toPort - 1)
			?: throw AiPlanExecutionException("'${operation.to}' has no input ${operation.toPort}.")

		val existingEdge = graphView.getEdgeView(originPort)
		if (existingEdge != null) {
			// Antares represents fan-out by branching from the existing wire, not by attaching a
			// second net to the source port. SplitEdgeViewCommand inserts the required junction.
			val split = fanOutSplits.getOrPut(operation.from to operation.fromPort) {
				fanOutSplitAllocator.allocate(existingEdge, remainingConnections)
			}.removeFirst()
			val branch = GraphViewModule.getEdgeViewFactory()
				.createEdgeView<DigitalSignal>(graphView)
				.addSegmentPoint(split.location)
				.addSegmentPoint(destination.getPortConnectionPoint(destinationPort))
			editor.commandManager.execute(SplitEdgeViewCommand(
				editor = editor,
				connectService = GraphViewModule.graphViewConnectService,
				splitEdgeViewId = existingEdge.id,
				segmentIndex = split.segmentIndex,
				splitLocation = split.location,
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

	private fun locationOf(operation: AiOperation.AddComponent, offset: Point2D): Point2D =
		Point2D(snapToGrid(operation.x ?: 0), snapToGrid(operation.y ?: 0)).add(offset)

	private fun equalizeColumnGaps(plan: AiValidatedPlan): AiValidatedPlan {
		val operations = mutableListOf<AiOperation>()
		val segment = mutableListOf<AiOperation>()
		plan.operations.forEach { operation ->
			if (operation is AiOperation.ClearCircuit) {
				operations.addAll(equalizeSegmentColumnGaps(segment))
				segment.clear()
				operations.add(operation)
			} else {
				segment.add(operation)
			}
		}
		operations.addAll(equalizeSegmentColumnGaps(segment))
		return plan.copy(operations = operations)
	}

	private fun alignSourcePorts(plan: AiValidatedPlan): AiValidatedPlan {
		val operations = plan.operations.toMutableList()
		val additions = operations.filterIsInstance<AiOperation.AddComponent>()
		val additionsById = additions.associateBy { it.ref.id }
		val viewsById = additions.associate { operation ->
			operation.ref.id to createVerticeView(operation).apply {
				location = Point2D(operation.x ?: 0, operation.y ?: 0)
			}
		}
		val aligned = mutableSetOf<String>()

		operations.filterIsInstance<AiOperation.Connect>().forEach { connection ->
			val from = (connection.from as? AiRef.New)?.id ?: return@forEach
			val to = (connection.to as? AiRef.New)?.id ?: return@forEach
			val source = additionsById[from] ?: return@forEach
			if (!isInputComponent(source.type) || !aligned.add(from)) {
				return@forEach
			}
			val sourceView = viewsById.getValue(from)
			val destinationView = viewsById[to] ?: return@forEach
			val sourcePort = sourceView.vertice.getOutputs().filter { it.portType == PortType.OUTPUT }
				.getOrNull(connection.fromPort - 1) ?: return@forEach
			val destinationPort = destinationView.vertice.getInputs().filter { it.portType == PortType.INPUT }
				.getOrNull(connection.toPort - 1) ?: return@forEach
			val sourceY = sourceView.getPortConnectionPoint(sourcePort).y
			val destinationY = destinationView.getPortConnectionPoint(destinationPort).y
			val y = (source.y ?: 0) + snapToGrid((destinationY - sourceY).roundToInt())
			val index = operations.indexOfFirst { it === source }
			operations[index] = source.copy(y = y)
		}

		return plan.copy(operations = operations)
	}

	private fun equalizeSegmentColumnGaps(operations: List<AiOperation>): List<AiOperation> {
		val components = operations.filterIsInstance<AiOperation.AddComponent>()
		val columns = components.groupBy { it.x ?: 0 }.toSortedMap().map { (x, column) ->
			val bounds = column.map { operation ->
				createVerticeView(operation).apply {
					location = Point2D(operation.x ?: 0, operation.y ?: 0)
				}.boundingBox
			}
			ColumnBounds(x, bounds.minOf { it.x }, bounds.maxOf { it.x + it.width })
		}
		if (columns.size < 3) {
			return operations
		}

		val totalSpan = columns.last().maxX - columns.first().minX
		val gap = maxOf(0.0, (totalSpan - columns.sumOf { it.width }) / (columns.size - 1))
		val shifts = mutableMapOf<Int, Int>()
		var nextMinX = columns.first().minX
		columns.forEach { column ->
			val shift = snapToGrid((nextMinX - column.minX).roundToInt())
			shifts[column.x] = shift
			nextMinX = column.maxX + shift + gap
		}

		return operations.map { operation ->
			if (operation is AiOperation.AddComponent) {
				operation.copy(x = (operation.x ?: 0) + shifts.getValue(operation.x ?: 0))
			} else {
				operation
			}
		}
	}

	private fun placementOffset(plan: AiValidatedPlan, graphView: GraphView): Point2D {
		val segmentOperations = plan.operations.takeWhile { it !is AiOperation.ClearCircuit }
		val additions = segmentOperations.filterIsInstance<AiOperation.AddComponent>()
		val existing = graphView.getVerticeViews()
		if (additions.isEmpty() || existing.isEmpty()) {
			return Point2D.ZERO
		}

		val existingBounds = existing.map { it.boundingBox }
		val newBounds = additions.map { operation ->
			createVerticeView(operation).apply {
				location = Point2D(operation.x ?: 0, operation.y ?: 0)
			}.boundingBox
		}
		alignedTerminalOffset(additions, existing, newBounds)?.let { return it }
		val connections = segmentOperations.filterIsInstance<AiOperation.Connect>()
		val feedsExisting = connections.any { it.from is AiRef.New && it.to is AiRef.Existing }
		val fedByExisting = connections.any { it.from is AiRef.Existing && it.to is AiRef.New }

		// Circuits flow left to right: new components that only feed into the existing circuit
		// belong on its input side, everything else continues to the right of its outputs.
		val x = if (feedsExisting && !fedByExisting) {
			existingBounds.minOf { it.x } - EXISTING_CIRCUIT_MARGIN - newBounds.maxOf { it.x + it.width }
		} else {
			existingBounds.maxOf { it.x + it.width } + EXISTING_CIRCUIT_MARGIN - newBounds.minOf { it.x }
		}
		val y = existingBounds.minOf { it.y } - newBounds.minOf { it.y }
		return Point2D(snapToGrid(x.roundToInt()), snapToGrid(y.roundToInt()))
	}

	private fun alignedTerminalOffset(
		additions: List<AiOperation.AddComponent>,
		existing: List<VerticeView<*>>,
		newBounds: List<RectangularShape>,
	): Point2D? {
		val matches: (VerticeView<*>) -> Boolean = when {
			additions.all { isInputComponent(it.type) } -> ::isInputComponent
			additions.all { isOutputComponent(it.type) } -> ::isOutputComponent
			else -> return null
		}
		val column = existing.filter(matches)
		if (column.isEmpty()) {
			return null
		}
		val x = column.first().location.x - additions.first().x!!
		val y = column.maxOf { it.boundingBox.y + it.boundingBox.height } + EXISTING_CIRCUIT_MARGIN
			- newBounds.minOf { it.y }
		return Point2D(snapToGrid(x.roundToInt()), snapToGrid(y.roundToInt()))
	}

	private fun isInputComponent(type: AiComponentType): Boolean =
		type == AiComponentType.Input || type == AiComponentType.Switch

	private fun isOutputComponent(type: AiComponentType): Boolean =
		type == AiComponentType.Output || type == AiComponentType.Led

	private fun isInputComponent(view: VerticeView<*>): Boolean = when (val model = view.vertice) {
		is DigitalCircuitInOut -> model.portType == PortType.INPUT
		is Switch -> true
		else -> false
	}

	private fun isOutputComponent(view: VerticeView<*>): Boolean = when (val model = view.vertice) {
		is DigitalCircuitInOut -> model.portType == PortType.OUTPUT
		is LED -> true
		else -> false
	}

	private fun remainingConnections(
		operations: List<AiOperation>,
		index: Int,
		connection: AiOperation.Connect
	): Int = operations.subList(index, operations.size)
		.takeWhile { it !is AiOperation.ClearCircuit }
		.filterIsInstance<AiOperation.Connect>()
		.count { it.from == connection.from && it.fromPort == connection.fromPort }

	private fun segmentLengths(edgeView: EdgeView<*>): List<Double> =
		(0 until edgeView.segmentPointCount - 1).map { edgeView.polyline.getSegmentLength(it) }

	private fun splitLocationAt(edgeView: EdgeView<*>, segmentLengths: List<Double>, distance: Double): EdgeSplit {
		var remaining = distance
		segmentLengths.forEachIndexed { segmentIndex, segmentLength ->
			if (remaining <= segmentLength || segmentIndex == segmentLengths.lastIndex) {
				val start = edgeView.getSegmentPoint(segmentIndex)
				val end = edgeView.getSegmentPoint(segmentIndex + 1)
				val ratio = if (segmentLength == 0.0) 0.5 else remaining / segmentLength
				val location = Point2D(
					start.x + (end.x - start.x) * ratio,
					start.y + (end.y - start.y) * ratio)
				return EdgeSplit(segmentIndex, snapSplitLocation(edgeView, segmentIndex, location, start, end))
			}
			remaining -= segmentLength
		}
		throw AiPlanExecutionException("Cannot split an empty wire.")
	}

	private fun snapSplitLocation(
		edgeView: EdgeView<*>,
		segmentIndex: Int,
		location: Point2D,
		start: Point2D,
		end: Point2D
	): Point2D {
		val snapped = when {
			edgeView.polyline.isSegmentHorizontal(segmentIndex) ->
				Point2D(snapToGrid(location.x.roundToInt()).toDouble(), location.y)
			edgeView.polyline.isSegmentVertical(segmentIndex) ->
				Point2D(location.x, snapToGrid(location.y.roundToInt()).toDouble())
			else -> location
		}
		return if (snapped == start || snapped == end) location else snapped
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
