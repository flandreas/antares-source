package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import org.junit.Test
import kotlin.test.assertEquals

class BidirectionalSplitterTest {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldSynchronizeInputSignal() {
		val bidiSplitter = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2)

		bidiSplitter.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.True, Bit.Undefined)), signalHandler)
		bidiSplitter.act(signalHandler, bidiSplitter.createActorData(bidiSplitter.getInput<DigitalSignal>(1)))

		val signal = DigitalSignalFactory.of(true)
		assertEquals(signal, bidiSplitter.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(signal, bidiSplitter.getInput<DigitalSignal>(2).getIncomingSignal())
	}
}