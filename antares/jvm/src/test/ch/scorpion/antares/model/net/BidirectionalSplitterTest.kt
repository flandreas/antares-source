package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class BidirectionalSplitterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@Test
	fun shouldSynchronizeInputSignal() {
		val bidiSplitter = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2)

		bidiSplitter.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(Bit.True, Bit.Undefined)), signalHandler)
		bidiSplitter.act(signalHandler, bidiSplitter.createActorData(bidiSplitter.getInput<DigitalSignal>(1)))

		val signal = Word.of(true)
		assertEquals(signal, bidiSplitter.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(signal, bidiSplitter.getInput<DigitalSignal>(2).getIncomingSignal())
	}
}