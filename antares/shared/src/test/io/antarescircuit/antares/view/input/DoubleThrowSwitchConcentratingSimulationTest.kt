package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.*

class DoubleThrowSwitchConcentratingSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var switchView: DoubleThrowSwitchView
	private lateinit var ledView: LEDView
	private lateinit var inputView1: DigitalCircuitInOutView
	private lateinit var inputView2: DigitalCircuitInOutView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		switchView = builder.addVerticeView(DoubleThrowSwitchView())
		ledView = builder.addVerticeView(LEDView())
		inputView1 = builder.addVerticeView(DigitalCircuitInOutView())
		inputView2 = builder.addVerticeView(DigitalCircuitInOutView())
		builder.connect(switchView, fromPort = switchView.model.getOutput(1), ledView)
		builder.connect(inputView1, switchView, toPort = switchView.model.getInput(2))
		builder.connect(inputView2, switchView, toPort = switchView.model.getInput(3))
		circuitView = builder.build()
	}

	@Test
	fun shouldForwardSignalsOnStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Bit.False), ledView.model.getInput<DigitalSignal>().getIncomingSignal())
	}

	@Test
	fun shouldRegisterAsNetTopologyChangerIfOff() {
		startSimulation()
		assertTrue(inputView1.model.getOutput<DigitalSignal>().combinedNets.first().netTopologyChanger.contains(switchView.model))
		assertTrue(inputView2.model.getOutput<DigitalSignal>().combinedNets.first().netTopologyChanger.contains(switchView.model))
	}

	@Test
	fun shouldRegisterAsNetTopologyChangerIfOn() {
		startSimulation()
		switchView.model.toggle(scheduler)
		assertTrue(inputView1.model.getOutput<DigitalSignal>().combinedNets.first().netTopologyChanger.contains(switchView.model))
		assertTrue(inputView2.model.getOutput<DigitalSignal>().combinedNets.first().netTopologyChanger.contains(switchView.model))
	}

	@Test
	fun shouldEmptySimulationQueueAtStartup() {
		startSimulation()
		repeat(5) {
			scheduler.execute()
		}
		assertTrue(scheduler.isQueueEmpty)
	}

	@Test
	fun shouldReformNetOnStateChange() {
		startSimulation()
		proceedUntilQueueIsEmpty()
		val oldCombinedNet = inputView1.model.getOutput<DigitalSignal>().combinedNets.first()

		switchView.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		val newCombinedNet = inputView1.model.getOutput<DigitalSignal>().combinedNets.first()
		assertNotSame(oldCombinedNet, newCombinedNet)
	}

	@Test
	fun shouldSwitch() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		inputView1.model.toggleBit(0, undefine = false, scheduler)
		proceedUntilQueueIsEmpty()

		switchView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()

		assertTrue(ledView.model.isOn)
		assertEquals(DigitalSignalFactory.of(Bit.False), switchView.model.getOutput<DigitalSignal>(3).net!!.signal)
	}
}