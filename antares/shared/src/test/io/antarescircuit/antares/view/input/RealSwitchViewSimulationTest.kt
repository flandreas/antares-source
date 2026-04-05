package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.net.PowerView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.*

class RealSwitchViewSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var realSwitchView: RealSwitchView
	private lateinit var powerView: PowerView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		realSwitchView = builder.addVerticeView(RealSwitchView())
		powerView = builder.addVerticeView(PowerView())
		ledView = builder.addVerticeView(LEDView())
		builder.connect(powerView, realSwitchView, toPort = realSwitchView.model.getInput(1))
		builder.connect(realSwitchView, fromPort = realSwitchView.model.getOutput(2), ledView)
		circuitView = builder.build()
	}

	@Test
	fun shouldRegisterAsNetTopologyChangerIfOff() {
		startSimulation()
		assertTrue(powerView.model.getOutput<DigitalSignal>().combinedNets.first().netTopologyChanger.contains(realSwitchView.model))
	}

	@Test
	fun shouldRegisterAsNetTopologyChangerIfOn() {
		startSimulation()
		realSwitchView.model.on(scheduler)
		assertTrue(powerView.model.getOutput<DigitalSignal>().combinedNets.first().netTopologyChanger.contains(realSwitchView.model))
	}

	@Test
	fun shouldReceiveConnectedSignalAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(true), realSwitchView.model.getInput<DigitalSignal>(1).getIncomingSignal())
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), realSwitchView.model.getInput<DigitalSignal>(2).getIncomingSignal())
	}

	@Test
	fun shouldReformNetOnStateChange() {
		startSimulation()
		proceedUntilQueueIsEmpty()
		val oldCombinedNet = powerView.model.getOutput<DigitalSignal>().combinedNets.first()

		realSwitchView.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		val newCombinedNet = powerView.model.getOutput<DigitalSignal>().combinedNets.first()
		assertNotSame(oldCombinedNet, newCombinedNet)
	}

	@Test
	fun shouldForwardSignalWhenOn() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		realSwitchView.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		assertTrue(ledView.model.isOn)
	}

	@Test
	fun shouldBeUndefinedWhenOff() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		realSwitchView.model.on(scheduler)
		proceedUntilQueueIsEmpty()

		realSwitchView.model.off(scheduler)
		proceedUntilQueueIsEmpty()

		assertFalse(ledView.model.isOn)
		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), realSwitchView.model.getOutput<DigitalSignal>(2).net!!.signal)
	}
}