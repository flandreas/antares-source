package io.antarescircuit.antares.ai

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.view.AbstractGraphViewEditingTest
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppServiceImpl
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Applies plans to a real [GraphView] driven by a real [io.antarescircuit.jabbah.edit.Editor],
 * which is what makes the undo behaviour meaningful to test.
 */
class AiPlanExecutorTest : AbstractGraphViewEditingTest() {

	private val executor = AiPlanExecutor()

	private val graphView: GraphView get() = editor.drawing as GraphView

	@BeforeTest
	fun setUp() {
		AntaresTestRule.configure()
		EditModule.drawingAppService = GraphViewAppServiceImpl(GraphViewCopyPasteService())
	}

	override fun setupCircuit() {
		editor.selectionTool.rubberBandHandler.delaySelectTimer = null
	}

	private fun add(id: String, type: AiComponentType, name: String? = null, inputCount: Int = type.defaultInputCount, x: Int? = 0, y: Int? = 0) =
		AiOperation.AddComponent(AiRef.New(id), type, name, inputCount, x, y, 0L)

	private fun connect(from: String, to: String, fromPort: Int = 1, toPort: Int = 1) =
		AiOperation.Connect(AiRef.New(from), fromPort, AiRef.New(to), toPort)

	/** A 2-input AND gate between two circuit inputs and one circuit output. */
	private fun andCircuitPlan() = AiValidatedPlan(
		reply = "Built an AND circuit",
		operations = listOf(
			add("a", AiComponentType.Input, name = "A", y = 0),
			add("b", AiComponentType.Input, name = "B", y = 60),
			add("g", AiComponentType.And, inputCount = 2, x = 100, y = 30),
			add("y", AiComponentType.Output, name = "Y", x = 200, y = 30),
			connect("a", "g", toPort = 1),
			connect("b", "g", toPort = 2),
			connect("g", "y")
		))

	@Test
	fun shouldBuildAndWireTheRequestedCircuit() {
		val result = executor.apply(andCircuitPlan(), editor)

		assertEquals(4, result.addedComponents)
		assertEquals(3, result.connections)
		assertEquals(0, result.deletedComponents)

		assertEquals(4, graphView.getVerticeViews().size)
		assertEquals(3, graphView.getEdgeViews().size)

		val gate = graphView.getVerticeViews().filterIsInstance<LogicGateView>().single()
		assertEquals(2, gate.vertice.inputCount)
		assertTrue(gate.vertice.isFullyConnected, "all gate ports should be wired")

		// The names of the circuit ports reach the model
		assertEquals(setOf("A", "B", "Y"), portNames())
	}

	@Test
	fun shouldShareANetWhenOneOutputFansOutToMultipleInputs() {
		val plan = AiValidatedPlan("Built a half adder", listOf(
			add("a", AiComponentType.Input, name = "A", y = 0),
			add("b", AiComponentType.Input, name = "B", y = 60),
			add("xor", AiComponentType.Xor, inputCount = 2, x = 100, y = 0),
			add("and", AiComponentType.And, inputCount = 2, x = 100, y = 60),
			add("sum", AiComponentType.Output, name = "SUM", x = 200, y = 0),
			add("carry", AiComponentType.Output, name = "CARRY", x = 200, y = 60),
			connect("a", "xor", toPort = 1),
			connect("a", "and", toPort = 1),
			connect("b", "xor", toPort = 2),
			connect("b", "and", toPort = 2),
			connect("xor", "sum"),
			connect("and", "carry")
		))

		val result = executor.apply(plan, editor)

		assertEquals(6, result.connections)
		assertEquals(2, graphView.getNodeViews().size, "each fan-out must insert a visible junction")
		assertEquals(4, graphView.netViewsCount, "each fanned-out input must use one shared net")
		assertTrue(graphView.getVerticeViews().all { it.vertice.isFullyConnected })
	}

	@Test
	fun shouldSnapCoordinatesToTheGrid() {
		executor.apply(
			AiValidatedPlan("", listOf(
				add("a", AiComponentType.Input, x = 103, y = 45),
				add("g", AiComponentType.And, inputCount = 2, x = -71, y = 98),
				connect("a", "g"))),
			editor)

		graphView.getVerticeViews().forEach {
			assertEquals(0, it.location.xInt % Look.GRID)
			assertEquals(0, it.location.yInt % Look.GRID)
		}
	}

	@Test
	fun shouldSpreadMultipleFanOutJunctionsAlongTheWire() {
		val plan = AiValidatedPlan("", listOf(
			add("source", AiComponentType.Input),
			add("first", AiComponentType.Led),
			add("second", AiComponentType.Led),
			add("third", AiComponentType.Led),
			add("fourth", AiComponentType.Led),
			add("fifth", AiComponentType.Led),
			add("sixth", AiComponentType.Led),
			connect("source", "first"),
			connect("source", "second"),
			connect("source", "third"),
			connect("source", "fourth"),
			connect("source", "fifth"),
			connect("source", "sixth")
		))

		executor.apply(plan, editor)

		val junctions = graphView.getNodeViews()
		assertEquals(5, junctions.size)
		assertEquals(5, junctions.map { it.location }.toSet().size, "the junctions must not sit on the same spot")
		junctions.forEach {
			assertEquals(0, it.location.xInt % Look.GRID)
			assertEquals(0, it.location.yInt % Look.GRID)
		}
		assertTrue(graphView.getEdgeViews().all { edge ->
			(0 until edge.segmentPointCount - 1).all { edge.polyline.isSegmentOrthogonal(it) }
		})
	}

	@Test
	fun shouldPlaceFollowUpComponentsBesideTheExistingCircuit() {
		executor.apply(AiValidatedPlan("", listOf(add("source", AiComponentType.Input))), editor)
		val source = graphView.getVerticeViews().single()
		val followUp = AiValidatedPlan("", listOf(
			add("gate", AiComponentType.And, name = "A very wide gate name"),
			AiOperation.Connect(AiRef.Existing(source.id), 1, AiRef.New("gate"), 1)
		))

		executor.apply(followUp, editor)

		val gate = graphView.getVerticeViews().single { it.id != source.id }
		assertTrue(gate.boundingBox.x > source.boundingBox.x + source.boundingBox.width)
		assertFalse(gate.boundingBox.intersects(source.boundingBox))
	}

	@Test
	fun shouldMakeTheWholePlanASingleUndoStep() {
		executor.apply(andCircuitPlan(), editor)
		assertEquals(4, graphView.getVerticeViews().size)

		editor.commandManager.undo()

		assertEquals(0, graphView.getVerticeViews().size)
		assertEquals(0, graphView.getEdgeViews().size)
		assertFalse(editor.commandManager.canUndo(), "a single undo should revert the complete plan")

		editor.commandManager.redo()

		assertEquals(4, graphView.getVerticeViews().size)
		assertEquals(3, graphView.getEdgeViews().size)
	}

	@Test
	fun shouldRollBackEverythingIfAnOperationFails() {
		executor.apply(AiValidatedPlan("", listOf(add("seed", AiComponentType.Input, name = "S"))), editor)
		assertEquals(1, graphView.getVerticeViews().size)

		val broken = AiValidatedPlan("", listOf(
			add("a", AiComponentType.Input, name = "A"),
			// References a component that is not part of the circuit, which validation would have caught
			AiOperation.Connect(AiRef.New("a"), 1, AiRef.Existing(9999), 1)
		))

		assertFailsWith<AiPlanExecutionException> { executor.apply(broken, editor) }

		assertEquals(1, graphView.getVerticeViews().size, "the failed plan must not leave anything behind")
		assertEquals(0, graphView.getEdgeViews().size)
	}

	/**
	 * Closes the loop: what the executor built must be described accurately enough by the context
	 * snapshot for the validator to accept a follow-up plan that references it.
	 */
	@Test
	fun shouldDescribeTheBuiltCircuitInTheContextSnapshot() {
		executor.apply(andCircuitPlan(), editor)

		val context = AiCircuitContext.of(graphView)

		assertEquals(4, context.components.size)
		assertEquals(3, context.connections.size)
		assertEquals(setOf("A", "B", "Y"), context.portNames())

		val gate = context.components.single { it.type.contains("AND", ignoreCase = true) }
		assertEquals(2, gate.inputCount)
		assertEquals(listOf(1, 2), gate.connectedInputs)

		// The gate output is taken; a follow-up plan may only wire the free inputs of the circuit
		val followUp = AiPlanValidator.validate(
			AiPlanDto(operations = listOf(
				AiOperationDto(op = AiOperationDto.OP_ADD_COMPONENT, id = "led", type = "led"),
				AiOperationDto(op = AiOperationDto.OP_CONNECT, from = gate.ref, to = "led"))),
			context)
		assertTrue(followUp.isValid, followUp.errors.toString())

		val rejected = AiPlanValidator.validate(
			AiPlanDto(operations = listOf(
				AiOperationDto(op = AiOperationDto.OP_ADD_COMPONENT, id = "c", type = "constant"),
				AiOperationDto(op = AiOperationDto.OP_CONNECT, from = "c", to = gate.ref, toPort = 1))),
			context)
		assertFalse(rejected.isValid, "wiring an occupied gate input should be rejected")
	}

	@Test
	fun shouldDeleteAndClearOnlyWhenAsked() {
		executor.apply(andCircuitPlan(), editor)
		val gateId = graphView.getVerticeViews().filterIsInstance<LogicGateView>().single().id

		val deleted = executor.apply(
			AiValidatedPlan("", listOf(AiOperation.DeleteComponent(AiRef.Existing(gateId)))),
			editor)

		assertEquals(1, deleted.deletedComponents)
		assertEquals(3, graphView.getVerticeViews().size)

		val cleared = executor.apply(AiValidatedPlan("", listOf(AiOperation.ClearCircuit)), editor)

		assertTrue(cleared.deletedComponents > 0)
		assertEquals(0, graphView.getVerticeViews().size)
	}

	private fun portNames(): Set<String> =
		graphView.getVerticeViews().mapNotNull { it.vertice.name }.toSet()
}
