package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.net.PowerView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.*

class RealSwitchViewSimulationTest : AbstractCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private lateinit var realSwitchView: RealSwitchView
	private lateinit var powerView: PowerView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
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
		realSwitchView.model.on(scheduler, circuitView)
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

		realSwitchView.model.on(scheduler, circuitView)
		proceedUntilQueueIsEmpty()

		val newCombinedNet = powerView.model.getOutput<DigitalSignal>().combinedNets.first()
		assertNotSame(oldCombinedNet, newCombinedNet)
	}

	@Test
	fun shouldForwardSignalWhenOn() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		realSwitchView.model.on(scheduler, circuitView)
		proceedUntilQueueIsEmpty()

		assertTrue(ledView.model.isOn)
	}

	@Test
	fun shouldBeUndefinedWhenOff() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		realSwitchView.model.on(scheduler, circuitView)
		proceedUntilQueueIsEmpty()

		realSwitchView.model.off(scheduler, circuitView)
		proceedUntilQueueIsEmpty()

		assertFalse(ledView.model.isOn)
		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), realSwitchView.model.getOutput<DigitalSignal>(2).net!!.signal)
	}
}