package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [Concentrator].
 */
class ConcentratorTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
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

        assertEquals(BitWidth.BW_4, (concentrator.getOutput<Any>().getOutgoingSignal() as Word).getBitWidth())
        assertEquals(6L, (concentrator.getOutput<Any>().getOutgoingSignal() as Word).getValue())
    }

    @Test
    fun shouldConcentrateSubwords() {
        val concentrator = Concentrator(BitWidth.BW_8, BranchCount.BC_4)

        concentrator.getInput<Any>(2).setIncomingSignal(Word.of(BitWidth.BW_2, 2L), signalHandler)
        concentrator.getInput<Any>(3).setIncomingSignal(Word.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.getInput<Any>(4).setIncomingSignal(Word.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.getInput<Any>(5).setIncomingSignal(Word.of(BitWidth.BW_2, 3L), signalHandler)
        concentrator.act(signalHandler, concentrator.createActorData(concentrator.getInput<DigitalSignal>(5)))

        assertEquals(BitWidth.BW_8, (concentrator.getOutput<Any>().getOutgoingSignal() as Word).getBitWidth())
        assertEquals(254L, (concentrator.getOutput<Any>().getOutgoingSignal() as Word).getValue())
    }
}