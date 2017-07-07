package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import org.junit.ClassRule
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


/**
 * Unit tests for [Splitter].
 */
class SplitterTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val signalHandler = ForwardSignalHandler()

    @Test
    fun shouldSplitToBits() {
        val splitter = Splitter(BitWidth.BW_4, 4)

        splitter.getInput<Any>().setIncomingSignal(Word.of(BitWidth.BW_4, 6L), signalHandler)
        splitter.act(signalHandler, splitter.createActorData(splitter.getInput<DigitalSignal>()))

        assertThat((splitter.getOutput<Any>(2).getOutgoingSignal() as Word).bitAt(0), `is`(Bit.False))
        assertThat((splitter.getOutput<Any>(3).getOutgoingSignal() as Word).bitAt(0), `is`(Bit.True))
        assertThat((splitter.getOutput<Any>(4).getOutgoingSignal() as Word).bitAt(0), `is`(Bit.True))
        assertThat((splitter.getOutput<Any>(5).getOutgoingSignal() as Word).bitAt(0), `is`(Bit.False))
    }

    @Test
    fun shouldSplitToSubwords() {
        val splitter = Splitter(BitWidth.BW_8, 4)

        splitter.getInput<Any>().setIncomingSignal(Word.of(BitWidth.BW_8, 255L), signalHandler)
        splitter.act(signalHandler, splitter.createActorData(splitter.getInput<DigitalSignal>()))

        assertThat((splitter.getOutput<Any>(2).getOutgoingSignal() as Word).getBitWidth(), `is`(BitWidth.BW_2))
        assertThat((splitter.getOutput<Any>(2).getOutgoingSignal() as Word).getValue(), `is`(3L))

        assertThat((splitter.getOutput<Any>(3).getOutgoingSignal() as Word).getBitWidth(), `is`(BitWidth.BW_2))
        assertThat((splitter.getOutput<Any>(3).getOutgoingSignal() as Word).getValue(), `is`(3L))

        assertThat((splitter.getOutput<Any>(4).getOutgoingSignal() as Word).getBitWidth(), `is`(BitWidth.BW_2))
        assertThat((splitter.getOutput<Any>(4).getOutgoingSignal() as Word).getValue(), `is`(3L))

        assertThat((splitter.getOutput<Any>(5).getOutgoingSignal() as Word).getBitWidth(), `is`(BitWidth.BW_2))
        assertThat((splitter.getOutput<Any>(5).getOutgoingSignal() as Word).getValue(), `is`(3L))
    }
}