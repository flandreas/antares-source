package ch.scorpion.antares.view.input

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DoubleThrowSwitchSplittingSimulationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var doubleThrowSwitchView: DoubleThrowSwitchView
	private lateinit var buttonView: SwitchView
	private lateinit var ledView1: LEDView
	private lateinit var ledView2: LEDView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)
		buttonView =  builder.addVerticeView(SwitchView())
		doubleThrowSwitchView = builder.addVerticeView(DoubleThrowSwitchView())
		ledView1 = builder.addVerticeView(LEDView())
		ledView2 = builder.addVerticeView(LEDView())
		builder.connect(buttonView, doubleThrowSwitchView, toPort = doubleThrowSwitchView.model.getInput(1))
		builder.connect(doubleThrowSwitchView, fromPort = doubleThrowSwitchView.model.getOutput(2), ledView1)
		builder.connect(doubleThrowSwitchView, fromPort = doubleThrowSwitchView.model.getOutput(3), ledView2)
		circuitView = builder.build()
	}

	@Test
	fun shouldBlindOutputBeUndefinedAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), ledView1.model.getInput<DigitalSignal>().net!!.signal)
	}

	@Test
	fun shouldSwitch() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		buttonView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()
		assertTrue(ledView2.model.isOn)
		assertFalse(ledView1.model.isOn)

		doubleThrowSwitchView.model.toggle(scheduler)
		proceedUntilQueueIsEmpty()

		assertTrue(ledView1.model.isOn)
		assertFalse(ledView2.model.isOn)
		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), ledView2.model.getInput<DigitalSignal>().net!!.signal)
	}
}