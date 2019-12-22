package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
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
	fun shouldBeDisabledWhileWaiting() {
		switch.on(signalHandler)
		assertFalse(switch.enabled)

		switch.act(signalHandler, GraphActorDataImpl(null, Word.of(Bit.True)))
		assertTrue(switch.enabled)
	}

	@Test
	fun shouldNotSetOnWhileWaiting() {
		switch.on(signalHandler)
		switch.act(signalHandler, GraphActorDataImpl(null, Word.of(Bit.True)))
		switch.off(signalHandler)

		switch.on(signalHandler)
		assertFalse(switch.isOn)
		assertFalse(switch.enabled)
	}

	@Test
	fun shouldDelaySetOffWhileDisabled() {
		switch.on(signalHandler)
		switch.off(signalHandler)
		assertTrue(switch.isOn)
		assertFalse(switch.enabled)

		switch.act(signalHandler, GraphActorDataImpl(null, Word.of(Bit.True)))
		assertFalse(switch.isOn)
		assertFalse(switch.enabled)

		switch.act(signalHandler, GraphActorDataImpl(null, Word.of(Bit.False)))
		assertFalse(switch.isOn)
		assertTrue(switch.enabled)
	}
}