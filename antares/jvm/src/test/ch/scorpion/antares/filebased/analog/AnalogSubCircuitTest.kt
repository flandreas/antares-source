package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.*

/** Includes an analog inverter (made from a transistor) in a digital circuit.*/
class AnalogSubCircuitTest : AbstractFileBasedTest() {

	private lateinit var switch: SwitchView
	private lateinit var led: LEDView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("863e2174-9a4d-444c-9d85-3196512c18b2"))
		startSimulation()
		processUntilQueueIsEmpty()

		switch = openedCircuitView.getWithId(2) as SwitchView
		led = openedCircuitView.getWithId(3) as LEDView
	}

	@Test
	fun shouldInvertOnStartup() {
		assertFalse(switch.model.isOn)
		assertTrue(led.model.isOn)
	}

	@Test
	fun shouldInvert() {
		switch.model.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertTrue(switch.model.isOn)
		assertFalse(led.model.isOn)

		switch.model.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertFalse(switch.model.isOn)
		assertTrue(led.model.isOn)
	}
}