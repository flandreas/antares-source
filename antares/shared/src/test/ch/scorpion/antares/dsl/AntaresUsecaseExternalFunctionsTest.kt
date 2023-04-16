package ch.scorpion.antares.dsl

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.usecase.UsecaseImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestFailureException
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner
import io.mockk.every
import io.mockk.mockk
import kotlin.test.*

class AntaresUsecaseExternalFunctionsTest : AbstractCircuitTest() {

	private val issueCollector = IssueCollector()

	private lateinit var circuitView: GraphView

	private lateinit var view: DrawingView<GraphView>

	private lateinit var switch: SwitchView

	private lateinit var input: DigitalCircuitInOutView

	private lateinit var orGate: OrGateView

	private lateinit var led: LEDView

	private lateinit var output: DigitalCircuitInOutView

	private val applicationModeHolder = DummyApplicationModeHolder()

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	override fun setup() {
		super.setup()

		val builder = TestCircuitBuilder("bla", styleProvider, eventBus)
		switch = builder.addVerticeView(SwitchView())
		input = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INPUT, name = "I")))
		orGate = builder.addVerticeView(OrGateView())
		led = builder.addVerticeView(LEDView())
		output = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.OUTPUT)))

		builder.connect(input, orGate, orGate.vertice.getInput(1))
		builder.connect(switch, orGate, orGate.vertice.getInput(2))
		val edgeView = builder.connect(orGate, led)
		builder.split(edgeView, 0, Point2D.ZERO, output)

		circuitView = builder.build()

		val canvas: Canvas = mockk(relaxed = true)

		view = DrawingViewImpl(
			drawing = circuitView as Drawing<Component>,
			transformFactory = { AffineTransformImpl() },
		) as DrawingView<GraphView>
		view.canvas = canvas

		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
	}

	@Test
	fun shouldPressButton() {
		val usecase = UsecaseImpl("PressButton", "pressButtonAt(10000, 1)")
		getCircuitView().usecases.add(usecase)
		val runner = UsecaseRunner(usecase, getCircuitView(), scheduler, applicationModeHolder)

		runner.run()
		proceedUntilQueueIsEmpty()

		assertEquals(0, issueCollector.size)
		assertTrue(switch.model.isOn)
	}

	@Test
	fun shouldApplyClock() {
		val usecase = UsecaseImpl("ApplyClock", "applyClock(\"I\", 1000)")
		getCircuitView().usecases.add(usecase)
		val runner = UsecaseRunner(usecase, getCircuitView(), scheduler, applicationModeHolder)
		runner.run()

		proceedToNanos(10_000)
		assertEquals(0, issueCollector.size)

		assertEquals(DigitalSignalFactory.of(false), input.model.getOutput<DigitalSignal>().getOutgoingSignal() as DigitalSignal)

		proceedToNanos(10_500)
		assertEquals(DigitalSignalFactory.of(true), input.model.getOutput<DigitalSignal>().getOutgoingSignal() as DigitalSignal)

		proceedToNanos(11_000)
		assertEquals(DigitalSignalFactory.of(false), input.model.getOutput<DigitalSignal>().getOutgoingSignal() as DigitalSignal)
	}

	@Test
	fun shouldAssertLedOn() {
		val usecase = UsecaseImpl(
			"AssertLedOn",
			"pressButtonAt(10000, 1)",
			"assertLedOnAt(20000, 4)")
		getCircuitView().usecases.add(usecase)
		val runner = UsecaseTestRunner(listOf(usecase), circuitView, scheduler, applicationModeHolder, throwFailureException = true)
		runner.run()

		proceedToNanos(20_000)
		assertEquals(0, issueCollector.size)
	}

	@Test
	fun shouldFailToAssertLedOn() {
		val usecase = UsecaseImpl(
			"AssertLedOn",
			"pressButtonAt(30000, 1)",
			"assertLedOnAt(20000, 4)")
		val runner = UsecaseTestRunner(listOf(usecase), circuitView, scheduler, applicationModeHolder, throwFailureException = true)
		getCircuitView().usecases.add(usecase)
		runner.run()

		assertFailsWith<UsecaseTestFailureException> {
			proceedToNanos(20_000)
			assertEquals(0, issueCollector.size)
		}
	}

	private inner class DummyApplicationModeHolder : ApplicationModeHolder {

		private var _currentMode: ApplicationMode = ApplicationMode.EDIT
		override val currentMode: ApplicationMode get() = _currentMode

		override fun dispose() { }

		override fun setMode(mode: ApplicationMode, after: () -> Unit) {
			if (mode.isExecute()) {
				startSimulation()
			} else {
				stopSimulation()
			}
			after.invoke()
		}

		override fun updateEditorEditability() { }
	}
}