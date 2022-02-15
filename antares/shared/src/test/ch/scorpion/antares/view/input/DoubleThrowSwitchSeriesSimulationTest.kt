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

class DoubleThrowSwitchSeriesSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var switch1: DoubleThrowSwitchView
	private lateinit var switch2: DoubleThrowSwitchView
	private lateinit var buttonView: SwitchView
	private lateinit var ledView1: LEDView
	private lateinit var ledView2: LEDView
	private lateinit var ledView3: LEDView

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		buttonView =  builder.addVerticeView(SwitchView())
		switch1 = builder.addVerticeView(DoubleThrowSwitchView())
		switch2 = builder.addVerticeView(DoubleThrowSwitchView())
		ledView1 = builder.addVerticeView(LEDView())
		ledView2 = builder.addVerticeView(LEDView())
		ledView3 = builder.addVerticeView(LEDView())

		builder.connect(buttonView, switch1, toPort = switch1.model.getInput(1))
		builder.connect(switch1, fromPort = switch1.model.getOutput(2), ledView1)
		builder.connect(switch1, fromPort = switch1.model.getOutput(3), switch2, toPort = switch2.model.getInput(1))
		builder.connect(switch2, fromPort = switch2.model.getOutput(2), ledView2)
		builder.connect(switch2, fromPort = switch2.model.getOutput(3), ledView3)

		circuitView = builder.build()
	}

	@Test
	fun combinedNetShouldContainAllNetTopologyChanger() {
		startSimulation()

		with(buttonView.model.getOutput<DigitalSignal>().combinedNets) {
			any { it.netTopologyChanger.contains(switch1.model) }
			any { it.netTopologyChanger.contains(switch2.model) }
		}
		assertTrue(switch1.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))
		assertTrue(switch2.model.containsNetTopologyChangeListener(buttonView.model.getOutput<DigitalSignal>(1) as PortImpl<*>))
	}

	@Test
	fun shouldSwitch() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		buttonView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()
		switch1.model.toggle(scheduler)

		proceedUntilQueueIsEmpty()

		assertTrue(ledView1.model.isOn)
		assertFalse(ledView2.model.isOn)
		assertFalse(ledView3.model.isOn)
	}
}