package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [TriStateBufferGate].
 */
class TriStateBufferGateTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    private val signalHandler = ForwardSignalHandler()

    // Positive

    @Test
    fun shouldForwardInputWhenEnabled() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.POSITIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(true), signalHandler)

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)
        assertEquals(Word.of(true), gate.getOutput<Word>().getOutgoingSignal())

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(false), signalHandler)
        assertEquals(Word.of(false), gate.getOutput<Word>().getOutgoingSignal())

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        assertEquals(Word.of(Bit.Undefined), gate.getOutput<Word>().getOutgoingSignal())
    }

    @Test
    fun shouldOutputUndefinedWhenDisabled() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.POSITIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(false), signalHandler)
        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)

        assertEquals(Word.of(Bit.Undefined), gate.getOutput<Word>().getOutgoingSignal())
    }

    // Negative

    @Test
    fun shouldForwardInputWhenEnabledNegative() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.NEGATIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(false), signalHandler)

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)
        assertEquals(Word.of(true), gate.getOutput<Word>().getOutgoingSignal())

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(false), signalHandler)
        assertEquals(Word.of(false), gate.getOutput<Word>().getOutgoingSignal())

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        assertEquals(Word.of(Bit.Undefined), gate.getOutput<Word>().getOutgoingSignal())

    }

    @Test
    fun shouldOutputUndefinedWhenDisabledNegative() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.NEGATIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(true), signalHandler)
        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)

        assertEquals(Word.of(Bit.Undefined), gate.getOutput<Word>().getOutgoingSignal())
    }
}
