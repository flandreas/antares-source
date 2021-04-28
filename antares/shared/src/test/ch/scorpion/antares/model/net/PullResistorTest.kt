package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class PullResistorTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = mockk<SignalHandler>()

	@Test
	fun shouldWithdrawWeakOutput() {
		val pullResistor = PullResistor(BitWidth.BW_2, PullDirection.LOW)
		pullResistor.getOutputPort().setOutgoingSignal(Word(listOf(False, False)), signalHandler)

		pullResistor.withdrawWeakOutput(Word(listOf(True, Undefined)), pullResistor.getOutputPort(), signalHandler)

		assertEquals(Word(listOf(Undefined, False)), pullResistor.getOutputPort().getOutgoingSignal())
	}

	@Test
	fun shouldActivateWeakOutput() {
		val pullResistor = PullResistor(BitWidth.BW_2, PullDirection.LOW)
		pullResistor.getOutputPort().setOutgoingSignal(Word(listOf(Undefined, Undefined)), signalHandler)

		val result = pullResistor.activateWeakOutput(Word(listOf(True, Undefined)), pullResistor.getOutputPort(), signalHandler)

		assertEquals(Word(listOf(Undefined, False)), pullResistor.getOutputPort().getOutgoingSignal())
		assertEquals(Word(listOf(True, False)), result)
	}
}