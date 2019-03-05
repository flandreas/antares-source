package ch.scorpion.antares.script

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.view.SimpleViewPainter
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeHolder
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import io.mockk.every
import io.mockk.mockk
import kotlin.test.*

class AntaresScriptGatewayTest : AbstractCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView<GraphElementView<*>>

	private lateinit var view: DrawingView<GraphView<GraphElementView<*>>>

	private lateinit var switch: SwitchView

	private lateinit var input: CircuitInOutView

	private lateinit var orGate: OrGateView

	private lateinit var led: LEDView

	private lateinit var output: CircuitInOutView

	private val gateway = AntaresScriptGateway()

	@BeforeTest
	override fun setup() {
		super.setup()
		val builder = TestCircuitBuilder("bla", styleProvider, eventBus)
		switch = builder.addVerticeView(SwitchView())
		input = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(portType = PortType.INPUT)))
		orGate = builder.addVerticeView(OrGateView())
		led = builder.addVerticeView(LEDView())
		output = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(portType = PortType.OUTPUT)))

		builder.connect(input, orGate, orGate.vertice.getInput(1))
		builder.connect(switch, orGate, orGate.vertice.getInput(2))
		builder.connect(orGate, led)

		circuitView = builder.build()

		val canvas: Canvas = mockk(relaxed = true)

		view = DrawingViewImpl(
			drawing = circuitView as Drawing<Component>,
			canvas = canvas,
			transformFactory = { AffineTransformImpl() },
			viewPainterFactory = { SimpleViewPainter(it) }
		) as DrawingView<GraphView<GraphElementView<*>>>

		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
	}

	override fun getCircuitView(): GraphView<GraphElementView<*>> = circuitView

	/** ---- [CircuitViewBridge] */

	@Test
	fun shouldReturnCircuitModelName() {
		assertEquals("bla", gateway.exec(script("return circuit.name();"), view))
	}

	@Test
	fun shouldGetInputSignal() {
		assertEquals("0", gateway.exec(script("return circuit.input(\"I1\");"), view))
	}

	@Test
	fun shouldGetOutputSignal() {
		assertEquals("0", gateway.exec(script("return circuit.output(\"O1\");"), view))
	}

	@Test
	fun shouldGetElement() {
		assertEquals(1, gateway.exec(script("return circuit.elem(1).id();"), view))
	}

	@Test
	fun shouldGetScenarioID() {
		view.drawing.currentScenario = ScenarioImpl("test")
		assertEquals("0", gateway.exec(script("return circuit.scenario();"), view))
	}

	@Test
	fun shouldHighlight() {
		gateway.exec(script("circuit.highlight(3);"), view)
		assertTrue(view.highlighter.isHighlighted(view.drawing.getWithId(3) as Component))
	}

	@Test
	fun shouldUnhighlight() {
		view.highlighter.highlight(3)
		gateway.exec(script("circuit.unhighlight();"), view)
		assertEquals(0, view.highlighter.highlightCount)
	}

	/** ---- [UsecaseBridge] */

	@Test
	fun shouldPressToggleButton() {
		val usecase = UsecaseImpl("PressButton", "circuit.pressButtonAt(10000, 1);")
		val runner = UsecaseRunner(usecase, view.drawing, scheduler, DummyApplicationModeHolder())
		runner.run()
		proceedToNanos(10_000)
		assertTrue(switch.model!!.isOn)
	}

	@Test
	fun shouldPressPushButton() {
		switch.toggle = false
		val usecase = UsecaseImpl("PressButton", "circuit.pressButtonAt(10000, 1);")
		val runner = UsecaseRunner(usecase, view.drawing, scheduler, DummyApplicationModeHolder())
		runner.run()
		proceedToNanos(10_000)
		assertTrue(switch.model!!.isOn)

		proceedToNanos(20_000)
		assertFalse(switch.model!!.isOn)
	}

	@Test
	fun shouldSetInput() {
		val usecase = UsecaseImpl("SetInput", "circuit.setInputAt(10000, 2, \"1\");")
		val runner = UsecaseRunner(usecase, view.drawing, scheduler, DummyApplicationModeHolder())
		runner.run()
		proceedToNanos(10_000)
		assertFalse(switch.model!!.isOn)
		assertEquals(Word.of(true), input.model!!.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldApplyClock() {
		val usecase = UsecaseImpl("ApplyClock", "circuit.applyClockAt(10000, 2, 1000);")
		val runner = UsecaseRunner(usecase, view.drawing, scheduler, DummyApplicationModeHolder())
		runner.run()

		proceedToNanos(10_000)
		assertEquals(Word.of(false), input.model!!.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToNanos(11_000)
		assertEquals(Word.of(true), input.model!!.getOutput<DigitalSignal>().getOutgoingSignal())

		proceedToNanos(12_000)
		assertEquals(Word.of(false), input.model!!.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	/** ---- [AntaresScriptGateway] */

	private fun script(code: String): Script {
		return Script(code, "Test", "Usecase")
	}

	private inner class DummyApplicationModeHolder : ApplicationModeHolder {

		private var _currentMode: ApplicationMode = ApplicationMode.EDIT
		override val currentMode: ApplicationMode get() = _currentMode

		override fun setMode(mode: ApplicationMode, after: () -> Unit) {
			if (mode.isExecute()) {
				startSimulation()
			} else {
				stopSimulation()
			}
			after.invoke()
		}
	}
}