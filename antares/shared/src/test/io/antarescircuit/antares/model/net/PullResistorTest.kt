package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class PullResistorTest {

	private val signalHandler = mock<SignalHandler>()

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldWithdrawWeakOutput() {
		val pullResistor = PullResistor(BitWidth.BW_2, PullDirection.LOW)
		pullResistor.getOutputPort().setOutgoingSignal(DigitalSignalFactory.ofBits(listOf(False, False)), signalHandler)

		pullResistor.withdrawWeakOutput(DigitalSignalFactory.ofBits(listOf(True, Undefined)), pullResistor.getOutputPort(), signalHandler)

		assertEquals(DigitalSignalFactory.ofBits(listOf(Undefined, False)), pullResistor.getOutputPort().getOutgoingSignal())
	}

	@Test
	fun shouldActivateWeakOutput() {
		val pullResistor = PullResistor(BitWidth.BW_2, PullDirection.LOW)
		pullResistor.getOutputPort().setOutgoingSignal(DigitalSignalFactory.ofBits(listOf(Undefined, Undefined)), signalHandler)

		val result = pullResistor.activateWeakOutput(DigitalSignalFactory.ofBits(listOf(True, Undefined)), pullResistor.getOutputPort(), signalHandler)

		assertEquals(DigitalSignalFactory.ofBits(listOf(Undefined, False)), pullResistor.getOutputPort().getOutgoingSignal())
		assertEquals(DigitalSignalFactory.ofBits(listOf(True, False)), result)
	}
}