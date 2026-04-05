package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SwitchTest {

	private val switch: Switch
	private val signalHandler: SignalHandler = mock(MockMode.autofill)

	init {
		AntaresTestRule.configure()
		switch = Switch()
	}

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