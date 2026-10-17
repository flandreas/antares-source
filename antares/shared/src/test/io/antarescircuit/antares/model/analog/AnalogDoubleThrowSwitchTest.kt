package io.antarescircuit.antares.model.analog

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalogDoubleThrowSwitchTest {

	private val signalHandler: SignalHandler = mock(MockMode.autofill)

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldBounceToRequestedState() {
		val switch = AnalogDoubleThrowSwitch().apply { bounce = true }
		switch.on(signalHandler)
		assertTrue(switch.isOnForView)
		assertFalse(switch.isOn)

		repeat(20) {
			if (!switch.enabled) {
				switch.act(signalHandler, switch.createActorData(null))
			}
		}

		assertTrue(switch.isOn)
		assertTrue(switch.enabled)

		switch.off(signalHandler)
		assertFalse(switch.isOnForView)
		assertTrue(switch.isOn)
		repeat(20) {
			if (!switch.enabled) {
				switch.act(signalHandler, switch.createActorData(null))
			}
		}

		assertFalse(switch.isOn)
		assertTrue(switch.enabled)
	}

	@Test
	fun shouldStoreBounceProperties() {
		val switch = AnalogDoubleThrowSwitch().apply {
			bounce = true
			bounceDuration = 250L
		}

		val clone = StorableCloner.clone(switch)

		assertEquals(250L, clone.bounceDuration)
		assertTrue(clone.bounce)
	}
}
