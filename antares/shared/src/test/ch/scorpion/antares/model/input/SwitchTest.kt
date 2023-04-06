package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.execution.SignalHandler
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SwitchTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val switch = Switch()
	private val signalHandler: SignalHandler = mockk(relaxed = true)

	@Test
	fun shouldDelaySwitchOn() {
		switch.on(signalHandler)
		assertFalse(switch.isOn)
		assertFalse(switch.enabled)

		switch.act(signalHandler, switch.createActorData(null))
		assertTrue(switch.isOn)
		assertTrue(switch.enabled)
	}

	@Test
	fun shouldDelaySwitchOff() {
		switch.on(signalHandler)
		switch.act(signalHandler, switch.createActorData(null))

		switch.off(signalHandler)
		assertTrue(switch.isOn)
		assertFalse(switch.enabled)
		switch.act(signalHandler, switch.createActorData(null))

		assertFalse(switch.isOn)
		assertTrue(switch.enabled)
	}
}