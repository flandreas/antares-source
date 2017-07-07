package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit tests for [TriStateBufferGate].
 */
class TriStateBufferGateTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val signalHandler = ForwardSignalHandler()

    // Positive

    @Test
    fun shouldForwardInputWhenEnabled() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.POSITIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(true), signalHandler)

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)
        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(true)))

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(false), signalHandler)
        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(false)))

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(Bit.Undefined)))
    }

    @Test
    fun shouldOutputUndefinedWhenDisabled() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.POSITIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(false), signalHandler)
        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)

        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(Bit.Undefined)))
    }

    // Negative

    @Test
    fun shouldForwardInputWhenEnabledNegative() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.NEGATIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(false), signalHandler)

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)
        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(true)))

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(false), signalHandler)
        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(false)))

        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(Bit.Undefined), signalHandler)
        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(Bit.Undefined)))

    }

    @Test
    fun shouldOutputUndefinedWhenDisabledNegative() {
        val gate = TriStateBufferGate(BitWidth.BW_1, Logic.NEGATIVE)
        gate.getInput<DigitalSignal>("EN").setIncomingSignal(Word.of(true), signalHandler)
        gate.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)

        assertThat(gate.getOutput<Word>().getOutgoingSignal(), `is`(Word.of(Bit.Undefined)))
    }
}
