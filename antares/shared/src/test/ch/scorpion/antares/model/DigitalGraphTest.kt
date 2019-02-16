package ch.scorpion.antares.model

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [DigitalGraph].
 */
class DigitalGraphTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldForwardTunnelSignal() {
		val signalHandler = ForwardSignalHandler()
		val testGraph = DigitalGraph(eventBus = mockk(relaxed = true))

		val tunnel1 = Tunnel("Test")
		testGraph.add(tunnel1)
		val tunnel2 = Tunnel("Test")
		testGraph.add(tunnel2)

		tunnel1.getInput<DigitalSignal>().setIncomingSignal(Word.of(true), signalHandler)

		assertEquals(Word.of(true), tunnel2.getOutput<DigitalSignal>().getOutgoingSignal() as Word)
	}
}