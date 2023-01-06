package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RealSwitchSeriesSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var buttonView: SwitchView
	private lateinit var switch1: RealSwitchView
	private lateinit var switch2: RealSwitchView
	private lateinit var ledView: LEDView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		buttonView =  builder.addVerticeView(SwitchView())
		switch1 = builder.addVerticeView(RealSwitchView())
		switch2 = builder.addVerticeView(RealSwitchView())
		ledView = builder.addVerticeView(LEDView())

		builder.connect(buttonView, switch1, toPort = switch1.model.getInput(1))
		builder.connect(switch1, fromPort = switch1.model.getOutput(2), switch2, toPort = switch2.model.getInput(1))
		builder.connect(switch2, fromPort = switch2.model.getOutput(2), ledView)

		circuitView = builder.build()
	}

	@Test
	fun combinedNetShouldContainReachableNetTopologyChangers() {
		startSimulation()

		with(buttonView.model.getOutput<DigitalSignal>().combinedNets) {
			assertTrue(any { it.netTopologyChanger.contains(switch1.model) })
			assertFalse(any { it.netTopologyChanger.contains(switch2.model) })
		}
		assertTrue(switch1.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))
		assertFalse(switch2.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))
	}

	@Test
	fun combinedNetShouldContainAllNetTopologyChangers() {
		startSimulation()

		switch1.model.toggle(scheduler, circuitView)

		with(buttonView.model.getOutput<DigitalSignal>().combinedNets) {
			assertTrue(any { it.netTopologyChanger.contains(switch1.model) })
			assertTrue(any { it.netTopologyChanger.contains(switch2.model) })
		}
		assertTrue(switch1.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))
		assertTrue(switch2.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))
	}

	@Test
	fun shouldPropagateSignal() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		buttonView.model.toggle(scheduler, circuitView)
		proceedUntilQueueIsEmpty()

		switch1.model.toggle(scheduler, circuitView)
		proceedUntilQueueIsEmpty()

		assertFalse(ledView.model.isOn)

		switch2.model.toggle(scheduler, circuitView)
		assertTrue(switch1.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))
		assertTrue(switch2.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))

		proceedUntilQueueIsEmpty()

		assertTrue(ledView.model.isOn)
	}
}