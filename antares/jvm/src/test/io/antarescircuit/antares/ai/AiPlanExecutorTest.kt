package io.antarescircuit.antares.ai

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.antares.view.AbstractGraphViewEditingTest
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.antares.view.input.ClockView
import io.antarescircuit.antares.view.net.ConstantView
import io.antarescircuit.antares.view.net.ConcentratorView
import io.antarescircuit.antares.view.net.SplitterView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.library.FileLibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppServiceImpl
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.math.abs
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
		val libraryDirectory = Files.createTempDirectory("ai-library")
		LibraryModule.userLibraryPersistenceService = FileLibraryPersistenceService(
			{ libraryDirectory.parent.absolutePathString() },
			libraryDirectory.name)
		LibraryModule.libraryHolder.l = LibraryImpl("AI test")
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
	fun shouldUseEqualVisualGapsAroundLogicGateColumns() {
		executor.apply(andCircuitPlan(), editor)

		val inputs = graphView.getVerticeViews().filter {
			(it.vertice as? DigitalCircuitInOut)?.portType == io.antarescircuit.jabbah.graph.model.PortType.INPUT
		}
		val gates = graphView.getVerticeViews().filterIsInstance<LogicGateView>()
		val outputs = graphView.getVerticeViews().filter {
			(it.vertice as? DigitalCircuitInOut)?.portType == io.antarescircuit.jabbah.graph.model.PortType.OUTPUT
		}
		val leftGap = gates.minOf { it.boundingBox.x } - inputs.maxOf { it.boundingBox.x + it.boundingBox.width }
		val rightGap = outputs.minOf { it.boundingBox.x } - gates.maxOf { it.boundingBox.x + it.boundingBox.width }

		assertTrue(abs(leftGap - rightGap) <= Look.GRID, "visible column gaps differ: $leftGap vs $rightGap")
	}

	@Test
	fun shouldAlignCircuitInputPortsWithTheirGateInputs() {
		executor.apply(andCircuitPlan(), editor)

		val inputs = graphView.getVerticeViews()
			.filter { (it.vertice as? DigitalCircuitInOut)?.portType == io.antarescircuit.jabbah.graph.model.PortType.INPUT }
			.associateBy { it.vertice.name }
		val gate = graphView.getVerticeViews().filterIsInstance<LogicGateView>().single()
		val gateInputs = gate.vertice.getInputs().filter {
			it.portType == io.antarescircuit.jabbah.graph.model.PortType.INPUT
		}

		listOf("A", "B").forEachIndexed { index, name ->
			val input = inputs.getValue(name)
			val outputPort = input.vertice.getOutputs().single()
			assertEquals(
				gate.getPortConnectionPoint(gateInputs[index]).yInt,
				input.getPortConnectionPoint(outputPort).yInt)
			val edge = graphView.getEdgeView(outputPort)!!
			assertTrue(edge.polyline.isSegmentHorizontal(0))
		}
	}

	@Test
	fun shouldBuildMultiBitCircuitPortsAndLogicGates() {
		val plan = AiValidatedPlan("Built an 8-bit buffer", listOf(
			add("a", AiComponentType.Input, name = "A").copy(bitWidth = 8),
			add("g", AiComponentType.Buffer).copy(bitWidth = 8),
			add("y", AiComponentType.Output, name = "Y").copy(bitWidth = 8),
			connect("a", "g"),
			connect("g", "y"),
		))

		executor.apply(plan, editor)

		val widths = graphView.getVerticeViews().map { view ->
			when (val model = view.vertice) {
				is DigitalCircuitInOut -> model.bitWidth.width
				is io.antarescircuit.antares.model.gate.AbstractLogicGate -> model.bitWidth.width
				else -> null
			}
		}
		assertEquals(listOf(8, 8, 8), widths)

		val context = AiCircuitContext.of(graphView)
		assertEquals(setOf(8), context.components.mapNotNull { it.bitWidth }.toSet())
	}

	@Test
	fun shouldBuildAndDescribeMultiBitConstant() {
		val constant = add("c", AiComponentType.Constant).copy(value = 5, bitWidth = 8)

		executor.apply(AiValidatedPlan("Added an 8-bit constant", listOf(constant)), editor)

		val model = graphView.getVerticeViews().filterIsInstance<ConstantView>().single().model
		assertEquals(5L, model.value.value)
		assertEquals(8, model.bitWidth.width)
		assertEquals(8, AiCircuitContext.of(graphView).components.single().bitWidth)
	}

	@Test
	fun shouldBuildAndDescribeSplitter() {
		val splitter = add("split", AiComponentType.Splitter).copy(
			bitWidth = 8,
			branchCount = 4,
			outputCount = 4)

		executor.apply(AiValidatedPlan("Added a splitter", listOf(splitter)), editor)

		val view = graphView.getVerticeViews().filterIsInstance<SplitterView>().single()
		val model = view.model
		assertEquals(8, model.bitWidth.width)
		assertEquals(4, model.branchCount.count)
		assertEquals(4, model.getOutputs().size)
		assertTrue(model.narrowSidePorts.all { it.bitWidth.width == 2 })
		assertEquals(Handedness.LEFT, view.handedness)
		val outputY = model.narrowSidePorts.map { view.getPortView(it)!!.location.yInt }
		assertEquals(outputY.sorted(), outputY, "bit 0 must be the top splitter output")

		val described = AiCircuitContext.of(graphView).components.single()
		assertEquals(AiComponentType.Splitter.id, described.type)
		assertEquals(8, described.bitWidth)
		assertEquals(4, described.branchCount)
		assertEquals(4, described.outputCount)
	}

	@Test
	fun shouldBuildAndDescribeConcentrator() {
		val concentrator = add("join", AiComponentType.Concentrator).copy(
			bitWidth = 8,
			branchCount = 4,
			inputCount = 4)

		executor.apply(AiValidatedPlan("Added a concentrator", listOf(concentrator)), editor)

		val view = graphView.getVerticeViews().filterIsInstance<ConcentratorView>().single()
		val model = view.model
		assertEquals(8, model.bitWidth.width)
		assertEquals(4, model.branchCount.count)
		assertEquals(4, model.getInputs().size)
		assertEquals(1, model.getOutputs().size)
		assertTrue(model.narrowSidePorts.all { it.bitWidth.width == 2 })
		assertEquals(Handedness.LEFT, view.handedness)
		val inputY = model.narrowSidePorts.map { view.getPortView(it)!!.location.yInt }
		assertEquals(inputY.sorted(), inputY, "bit 0 must be the top concentrator input")

		val described = AiCircuitContext.of(graphView).components.single()
		assertEquals(AiComponentType.Concentrator.id, described.type)
		assertEquals(8, described.bitWidth)
		assertEquals(4, described.branchCount)
		assertEquals(4, described.inputCount)
		assertEquals(1, described.outputCount)
	}

	@Test
	fun shouldBuildAndDescribeNegativeEnableTriStateBuffer() {
		val buffer = add("buffer", AiComponentType.TriStateBuffer).copy(
			bitWidth = 8,
			enableLogic = Logic.NEGATIVE)

		executor.apply(AiValidatedPlan("Added a tri-state buffer", listOf(buffer)), editor)

		val model = graphView.getVerticeViews().filterIsInstance<TriStateBufferGateView>().single().model
		assertEquals(8, model.bitWidth.width)
		assertEquals(Logic.NEGATIVE, model.enableLogic)
		assertEquals(8, model.getInputPort().bitWidth.width)
		assertEquals(1, model.getEnablePort().bitWidth.width)
		assertEquals(8, model.getOutputPort().bitWidth.width)

		val described = AiCircuitContext.of(graphView).components.single()
		assertEquals(AiComponentType.TriStateBuffer.id, described.type)
		assertEquals(8, described.bitWidth)
		assertEquals("negative", described.enableLogic)
		assertEquals(2, described.inputCount)
		assertEquals(1, described.outputCount)
	}

	@Test
	fun shouldBuildAndDescribeClockFrequency() {
		val clock = add("clk", AiComponentType.Clock, name = "CLK").copy(
			periodOrFrequency = MagnitudeValue(20, Magnitude.Mega, SIUnit.Hertz))

		executor.apply(AiValidatedPlan("Added a clock", listOf(clock)), editor)

		val model = graphView.getVerticeViews().filterIsInstance<ClockView>().single().model
		assertEquals("CLK", model.name)
		assertEquals(MagnitudeValue(20, Magnitude.Mega, SIUnit.Hertz), model.periodOrFrequency)
		assertEquals(0, model.getInputs().size)
		assertEquals(1, model.getOutputs().size)

		val described = AiCircuitContext.of(graphView).components.single()
		assertEquals(AiComponentType.Clock.id, described.type)
		assertEquals("20 MHz", described.periodOrFrequency)
		assertEquals(0, described.inputCount)
		assertEquals(1, described.outputCount)
	}

	@Test
	fun shouldAddAndConnectAvailableSubcircuitByMetaGraphUuid() {
		val metaGraph = TestLibraryBuilder().addNOP(LibraryModule.libraryHolder.library)
		val context = AiCircuitContext.of(graphView)
		val available = context.availableSubcircuits.single { it.uuid == metaGraph.uuid.id }
		assertEquals(1, available.inputPorts.size)
		assertEquals(1, available.outputPorts.size)

		val subcircuit = add("custom", AiComponentType.Subcircuit).copy(
			metaGraphUuid = metaGraph.uuid.id,
			inputCount = available.inputPorts.size,
			outputCount = available.outputPorts.size)
		val result = executor.apply(AiValidatedPlan("Added NOP", listOf(
			add("a", AiComponentType.Input, name = "A"),
			subcircuit,
			add("y", AiComponentType.Output, name = "Y"),
			connect("a", "custom"),
			connect("custom", "y"),
		)), editor)

		assertEquals(3, result.addedComponents)
		assertEquals(2, result.connections)
		val view = graphView.getVerticeViews().filterIsInstance<SubGraphVerticeView<*>>().single()
		assertEquals(metaGraph.uuid, view.subGraphVertice!!.graphUUID)
		assertEquals(2, view.getPortViews().size)
		assertTrue(view.vertice.isFullyConnected)

		val described = AiCircuitContext.of(graphView).components.single { it.ref == "#${view.id}" }
		assertEquals(AiComponentType.Subcircuit.id, described.type)
		assertEquals(metaGraph.uuid.id, described.metaGraphUuid)

		editor.commandManager.undo()
		assertTrue(graphView.getVerticeViews().isEmpty())
	}

	@Test
	fun shouldChangeConnectedCircuitBitWidthsAsOneUndoablePlan() {
		executor.apply(AiValidatedPlan("", listOf(
			add("a", AiComponentType.Input, name = "A"),
			add("g", AiComponentType.Not),
			add("y", AiComponentType.Output, name = "Y"),
			connect("a", "g"),
			connect("g", "y"),
		)), editor)
		val components = graphView.getVerticeViews().associateBy { it.vertice.name }
		val change = AiValidatedPlan("Changed to 8 bit", listOf(
			AiOperation.SetBitWidth(AiRef.Existing(components.getValue("A").id), 8),
			AiOperation.SetBitWidth(AiRef.Existing(components.getValue("Y").id), 8),
			AiOperation.SetBitWidth(
				AiRef.Existing(graphView.getVerticeViews().filterIsInstance<LogicGateView>().single().id), 8),
		))

		val result = executor.apply(change, editor)

		assertEquals(3, result.changedBitWidths)
		assertEquals(setOf(8), AiCircuitContext.of(graphView).components.mapNotNull { it.bitWidth }.toSet())

		editor.commandManager.undo()

		assertEquals(setOf(1), AiCircuitContext.of(graphView).components.mapNotNull { it.bitWidth }.toSet())
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
	fun shouldPlaceFollowUpLedsInTheExistingOutputColumn() {
		executor.apply(andCircuitPlan(), editor)
		val gate = graphView.getVerticeViews().filterIsInstance<LogicGateView>().single()
		val output = graphView.getVerticeViews().single { it.vertice.name == "Y" }

		executor.apply(AiValidatedPlan("", listOf(
			add("led", AiComponentType.Led),
			AiOperation.Connect(AiRef.Existing(gate.id), 1, AiRef.New("led"), 1),
		)), editor)

		val led = graphView.getVerticeViews().filterIsInstance<LEDView>().single()
		assertEquals(output.location.xInt, led.location.xInt)
		assertFalse(output.boundingBox.intersects(led.boundingBox))
	}

	@Test
	fun shouldStaggerFollowUpJunctionsAcrossParallelOutputWires() {
		executor.apply(AiValidatedPlan("", listOf(
			add("a", AiComponentType.Input, name = "A"),
			add("b", AiComponentType.Input, name = "B"),
			add("xor", AiComponentType.Xor),
			add("and", AiComponentType.And),
			add("sum", AiComponentType.Output, name = "SUM"),
			add("carry", AiComponentType.Output, name = "CARRY"),
			connect("a", "xor", toPort = 1),
			connect("a", "and", toPort = 1),
			connect("b", "xor", toPort = 2),
			connect("b", "and", toPort = 2),
			connect("xor", "sum"),
			connect("and", "carry"),
		)), editor)
		val gates = graphView.getVerticeViews().filterIsInstance<LogicGateView>()
		val existingNodeIds = graphView.getNodeViews().map { it.id }.toSet()

		executor.apply(AiValidatedPlan("", listOf(
			add("sumLed", AiComponentType.Led),
			add("carryLed", AiComponentType.Led),
			AiOperation.Connect(AiRef.Existing(gates[0].id), 1, AiRef.New("sumLed"), 1),
			AiOperation.Connect(AiRef.Existing(gates[1].id), 1, AiRef.New("carryLed"), 1),
		)), editor)

		val followUpJunctions = graphView.getNodeViews().filter { it.id !in existingNodeIds }
		assertEquals(2, followUpJunctions.size)
		assertEquals(2, followUpJunctions.map { it.location.xInt }.toSet().size)
	}

	@Test
	fun shouldPlaceFollowUpSwitchesInTheExistingInputColumn() {
		executor.apply(AiValidatedPlan("", listOf(
			add("input", AiComponentType.Input, name = "I"),
		)), editor)
		val input = graphView.getVerticeViews().single()

		executor.apply(AiValidatedPlan("", listOf(
			add("switch", AiComponentType.Switch),
		)), editor)

		val switch = graphView.getVerticeViews().single { it.id != input.id }
		assertEquals(input.location.xInt, switch.location.xInt)
		assertFalse(input.boundingBox.intersects(switch.boundingBox))
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
