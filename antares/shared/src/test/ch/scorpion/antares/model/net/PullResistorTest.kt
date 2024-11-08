package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.SignalHandler
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class PullResistorTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = mock<SignalHandler>()

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