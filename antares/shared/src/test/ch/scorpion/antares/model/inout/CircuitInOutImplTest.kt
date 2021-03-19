package ch.scorpion.antares.model.inout

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorDataImpl
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CircuitInOutImplTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val inout = CircuitInOutImpl()
	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@Test
	fun shouldBeDisabledWhileWaiting() {
		inout.setIncomingSignal(Word.of(true), signalHandler)
		assertFalse(inout.enabled)

		inout.act(signalHandler, GraphActorDataImpl(null, Word.of(true)))
		assertTrue(inout.enabled)
	}
}