package io.antarescircuit.antares.model.input

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.io.StorableCloner

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

	@Test
	fun shouldBounceToRequestedState() {
		switch.bounce = true
		switch.on(signalHandler)

		repeat(20) {
			if (!switch.enabled) {
				switch.act(signalHandler, switch.createActorData(null))
			}
		}

		assertTrue(switch.isOn)
		assertTrue(switch.enabled)
	}

	@Test
	fun shouldOutputUndefinedWhenOffIfConfigured() {
		switch.undefinedWhenOff = true

		switch.act(signalHandler, switch.createActorData(null))
		assertEquals(DigitalSignalFactory.of(Bit.Undefined), switch.getOutput<DigitalSignal>().getOutgoingSignal())

		switch.on(signalHandler)
		switch.act(signalHandler, switch.createActorData(null))
		assertEquals(DigitalSignalFactory.of(Bit.True), switch.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldBounceBetweenUndefinedAndOneWhenOffIsUndefined() {
		switch.undefinedWhenOff = true
		switch.bounce = true
		switch.on(signalHandler)

		val outputSignals = mutableSetOf<DigitalSignal?>()
		repeat(20) {
			switch.act(signalHandler, switch.createActorData(null))
			outputSignals += switch.getOutput<DigitalSignal>().getOutgoingSignal()
		}

		assertTrue(outputSignals.contains(DigitalSignalFactory.of(Bit.True)))
		assertTrue(outputSignals.contains(DigitalSignalFactory.of(Bit.Undefined)))
	}

	@Test
	fun shouldBounceBetweenZeroAndUndefinedWhenSwitchingOffAndOffIsUndefined() {
		switch.undefinedWhenOff = true
		switch.bounce = true
		switch.on(signalHandler)
		repeat(20) {
			switch.act(signalHandler, switch.createActorData(null))
		}
		switch.off(signalHandler)

		val outputSignals = mutableSetOf<DigitalSignal?>()
		repeat(20) {
			switch.act(signalHandler, switch.createActorData(null))
			outputSignals += switch.getOutput<DigitalSignal>().getOutgoingSignal()
		}

		assertTrue(outputSignals.contains(DigitalSignalFactory.of(Bit.False)))
		assertTrue(outputSignals.contains(DigitalSignalFactory.of(Bit.Undefined)))
		assertFalse(outputSignals.contains(DigitalSignalFactory.of(Bit.True)))
	}

	@Test
	fun shouldBounceBetweenZeroAndOneWhenSwitchingOffAndOffIsDefined() {
		switch.bounce = true
		switch.on(signalHandler)
		repeat(20) {
			switch.act(signalHandler, switch.createActorData(null))
		}
		switch.off(signalHandler)

		val outputSignals = mutableSetOf<DigitalSignal?>()
		repeat(20) {
			switch.act(signalHandler, switch.createActorData(null))
			outputSignals += switch.getOutput<DigitalSignal>().getOutgoingSignal()
		}

		assertTrue(outputSignals.contains(DigitalSignalFactory.of(Bit.False)))
		assertTrue(outputSignals.contains(DigitalSignalFactory.of(Bit.True)))
		assertFalse(outputSignals.contains(DigitalSignalFactory.of(Bit.Undefined)))
	}

	@Test
	fun shouldStoreBounceProperties() {
		switch.bounce = true
		switch.bounceDuration = 250L
		switch.undefinedWhenOff = true
		switch.minOnTime = 500L

		val clone = StorableCloner.clone(switch)

		assertEquals(250L, clone.bounceDuration)
		assertEquals(500L, clone.minOnTime)
		assertTrue(clone.bounce)
		assertTrue(clone.undefinedWhenOff)
	}
}
