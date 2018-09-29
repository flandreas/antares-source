package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit tests for [Concentrator].
 */
class ConcentratorTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val signalHandler = ForwardSignalHandler()

    @Test
    fun shouldConcentrateBits() {
        val concentrator = Concentrator(BitWidth.BW_4, BranchCount.BC_4)

        concentrator.getInput<Any>(2).setIncomingSignal(Word.of(false), signalHandler)
        concentrator.getInput<Any>(3).setIncomingSignal(Word.of(true), signalHandler)
        concentrator.getInput<Any>(4).setIncomingSignal(Word.of(true), signalHandler)
        concentrator.getInput<Any>(5).setIncomingSignal(Word.of(false), signalHandler)
        concentrator.act(signalHandler, concentrator.createActorData(concentrator.getInput<DigitalSignal>(5)))

        assertThat((concentrator.getOutput<Any>().getOutgoingSignal() as Word).getBitWidth(), `is`(BitWidth.BW_4))
        assertThat((concentrator.getOutput<Any>().getOutgoingSignal() as Word).getValue(), `is`(6L))
    }

    @Test
    fun shouldConcentrateSubwords() {
        val concentrator = Concentrator(BitWidth.BW_8, BranchCount.BC_4)

        concentrator.getInput<Any>(2).setIncomingSignal(Word.of(BitWidth.BW_2, 2L), signalHandler)
        concentrator.getInput<Any>(3).setIncomingSignal(Word.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.getInput<Any>(4).setIncomingSignal(Word.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.getInput<Any>(5).setIncomingSignal(Word.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.act(signalHandler, concentrator.createActorData(concentrator.getInput<DigitalSignal>(5)))

        assertThat((concentrator.getOutput<Any>().getOutgoingSignal() as Word).getBitWidth(), `is`(BitWidth.BW_8))
        assertThat((concentrator.getOutput<Any>().getOutgoingSignal() as Word).getValue(), `is`(254L))
    }
}