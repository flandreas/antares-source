package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.geom.AffineTransformImpl
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Canvas
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseImpl
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseRunner
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseTestFailureException
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseTestRunner
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.graph.view.GraphElementView
import kotlin.test.*

class AntaresUsecaseExternalFunctionsTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView

	private lateinit var view: DrawingView<GraphElementView<*>, GraphView>

	private lateinit var switch: SwitchView

	private lateinit var input: DigitalCircuitInOutView

	private lateinit var orGate: LogicGateView

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
		orGate = builder.addVerticeView(LogicGateView.orGateView())
		led = builder.addVerticeView(LEDView())
		output = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.OUTPUT)))

		builder.connect(input, orGate, orGate.vertice.getInput(1))
		builder.connect(switch, orGate, orGate.vertice.getInput(2))
		val edgeView = builder.connect(orGate, led)
		builder.split(edgeView, 0, Point2D.ZERO, output)

		circuitView = builder.build()

		val canvas: Canvas = mock(MockMode.autofill)
		every { canvas.dimension } returns Dimension2D(100, 100)

		view = DrawingViewImpl(
			drawing = circuitView as Drawing<Component>,
			transformFactory = { AffineTransformImpl() },
		) as DrawingView<GraphElementView<*>, GraphView>
		every { canvas.view } returns view

		view.canvas = canvas
	}

	@Test
	fun shouldPressButton() {
		val usecase = UsecaseImpl("PressButton", "pressButtonAt(10000, 1)")
		getCircuitView().usecases.add(usecase)
		val runner = UsecaseRunner(usecase, getCircuitView(), scheduler, applicationModeHolder)

		runner.run()
		proceedUntilQueueIsEmpty()

		assertNoIssues()
		assertTrue(switch.model.isOn)
	}

	@Test
	fun shouldApplyClock() {
		val usecase = UsecaseImpl("ApplyClock", "applyClock(\"I\", 1000)")
		getCircuitView().usecases.add(usecase)
		val runner = UsecaseRunner(usecase, getCircuitView(), scheduler, applicationModeHolder)
		runner.run()

		proceedToNanos(10_000)
		assertNoIssues()

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
		assertNoIssues()
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
			assertNoIssues()
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