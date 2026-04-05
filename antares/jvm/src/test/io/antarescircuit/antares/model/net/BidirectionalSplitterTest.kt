package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.jabbah.execution.SignalHandler
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