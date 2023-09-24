package ch.scorpion.antares.view.gate

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.NonUnaryLogicGate
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType.And
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.every
import io.mockk.mockk
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
	private val drawingView = mockk<DrawingView<GraphView>>()

	override fun getCircuitView(): GraphView = builder.graphView

	@BeforeTest
	fun setupCircuit() {
		every { drawingView.drawing } answers { getCircuitView() }

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