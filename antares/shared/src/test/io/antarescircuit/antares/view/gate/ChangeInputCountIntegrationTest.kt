package io.antarescircuit.antares.view.gate

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.And
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.app.AntaresGraphViewService
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.*

/**
 * Integration test for increasing and decreasing [PortCount] of [LogicGateView],
 * using an AND gate as an example.
 */
class ChangeInputCountIntegrationTest : AbstractCircuitTest() {

	private lateinit var builder: TestCircuitBuilder
	private lateinit var andGateView: LogicGateView
	private lateinit var switchView1: SwitchView
	private lateinit var switchView2: SwitchView
	private lateinit var switchView3: SwitchView
	private lateinit var ledView: LEDView
	private lateinit var evIn1: EdgeView<DigitalSignal>
	private lateinit var evIn2: EdgeView<DigitalSignal>
	private lateinit var evIn3: EdgeView<DigitalSignal>
	private lateinit var evOut: EdgeView<DigitalSignal>
	private val drawingViewBuilder = DrawingViewMockBuilder().withDrawingAccessor(::getCircuitView)

	override fun getCircuitView(): GraphView = builder.graphView

	private val drawingView: DrawingView<GraphView> get() = drawingViewBuilder.build<Component>() as DrawingView<GraphView>

	@BeforeTest
	fun setupCircuit() {
		val model = NonUnaryLogicGate(And, PortCount.THREE)
		builder = TestCircuitBuilder("test", styleProvider, eventBus)
		andGateView = builder.addVerticeView(LogicGateView(gate = model))
		switchView1 = builder.addVerticeView(SwitchView())
		switchView2 = builder.addVerticeView(SwitchView())
		switchView3 = builder.addVerticeView(SwitchView())
		ledView = builder.addVerticeView(LEDView())

		evIn1 = builder.connect(switchView1, andGateView, toPort = andGateView.model.getInput(1))
		evIn2 = builder.connect(switchView2, andGateView, toPort = andGateView.model.getInput(2))
		evIn3 = builder.connect(switchView3, andGateView, toPort = andGateView.model.getInput(3))
		evOut = builder.connect(andGateView, ledView)
	}

	@Test
	fun shouldIncreaseInputCount() {
		(GraphViewModule.graphViewAppService as AntaresGraphViewService).changeInputCount(andGateView, PortCount.FOUR, drawingView)

		assertTrue(evIn1.model.isConnectedWith(andGateView.model.getInput(1)))
		assertTrue(evIn2.model.isConnectedWith(andGateView.model.getInput(2)))
		assertTrue(evIn3.model.isConnectedWith(andGateView.model.getInput(3)))
		assertTrue(evOut.model.isConnectedWith(andGateView.model.getOutput()))

		assertEquals(4 + 1, andGateView.getPortViews().size)
		assertSame(andGateView.model.getInput(1), evIn1.destination!!.port)
		assertSame(andGateView.model.getInput(2), evIn2.destination!!.port)
		assertSame(andGateView.model.getInput(3), evIn3.destination!!.port)
		assertSame(andGateView.model.getOutput(), evOut.origin!!.port)
	}

	@Test
	fun shouldDecreaseInputCount() {
		(GraphViewModule.graphViewAppService as AntaresGraphViewService).changeInputCount(andGateView, PortCount.TWO, drawingView)

		assertEquals(2, andGateView.model.inputCount)
		assertEquals(1, andGateView.model.outputCount)
		assertEquals(3, andGateView.model.getOutput<DigitalSignal>().portId)
		assertTrue(evIn1.model.isConnectedWith(andGateView.model.getInput(1)))
		assertTrue(evIn2.model.isConnectedWith(andGateView.model.getInput(2)))
		assertEquals(1, evIn3.net!!.portsCount)

		assertEquals(2 + 1, andGateView.getPortViews().size)
		assertSame(andGateView.model.getInput(1), evIn1.destination!!.port)
		assertSame(andGateView.model.getInput(2), evIn2.destination!!.port)
		assertNull(evIn3.destination?.port)
	}

	@Test
	fun shouldDecreaseInputCountBy2() {
		val model = NonUnaryLogicGate(And, PortCount.FOUR)
		val andGateView2 = LogicGateView(gate = model)

		(GraphViewModule.graphViewAppService as AntaresGraphViewService).changeInputCount(andGateView2, PortCount.TWO, drawingView)

		assertEquals(2, andGateView2.model.inputCount)
	}
}