package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import com.nhaarman.mockitokotlin2.mock
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit tests for [RAM].
 */
class RAMCalculatorTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val calculator = RAMCalculator()
    private val signalHandler: SignalHandler = mock()

    /** ---- Clocked tests */

    @Test
    fun shouldWrite() {
        val ram = createRam(true)
        ram.getChipSelectInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
        ram.getDataPort().setIncomingSignal(Word.of(BitWidth.BW_8, 99L), signalHandler)
        ram.getClockInput()!!.setIncomingSignal(Word.of(true), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getClockInput()!!) as GraphActorData, signalHandler)

        assertThat(ram.read(1), `is`(99L))
    }

    @Test
    fun shouldNotWriteWhenNotEnabled() {
        val ram = createRam(true)
        ram.getChipSelectInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getWriteInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
        ram.getDataPort().setIncomingSignal(Word.of(BitWidth.BW_8, 99L), signalHandler)
        ram.getClockInput()!!.setIncomingSignal(Word.of(true), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getClockInput()) as GraphActorData, signalHandler)

        assertThat(ram.read(1), `is`(0L))
    }

    @Test
    fun shouldUndefineDataWhenWriteEnabled() {
        val ram = createRam(true)
        ram.getChipSelectInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getClockInput()) as GraphActorData, signalHandler)

        assertThat(ram.getDataPort().getOutgoingSignal() as Word, `is`(Word.undefined(BitWidth.BW_8)))
    }

    @Test
    fun shouldRead() {
        val ram = createRam(true)
        ram.write(1, 99)
        ram.getChipSelectInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()) as GraphActorData, signalHandler)

        assertThat(ram.getDataPort().getOutgoingSignal() as Word, `is`(Word.of(BitWidth.BW_8, 99L)))
    }

    /** ---- Unclocked tests */

    @Test
    fun shouldWriteUnclocked() {
        val ram = createRam(false)
        ram.getChipSelectInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
        ram.getDataPort().setIncomingSignal(Word.of(BitWidth.BW_8, 99L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getDataPort()) as GraphActorData, signalHandler)

        assertThat(ram.read(1), `is`(99L))
    }

    @Test
    fun shouldNotReadWhenNotEnabled() {
        val ram = createRam(false)
        ram.write(1, 99)
        ram.getChipSelectInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getWriteInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()) as GraphActorData, signalHandler)

        assertThat(ram.getDataPort().getOutgoingSignal() as Word, `is`(Word.undefined(BitWidth.BW_8)))
    }

    @Test
    fun shouldReadUnclocked() {
        val ram = createRam(false)
        ram.write(1, 99)
        ram.getChipSelectInput().setIncomingSignal(Word.of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()) as GraphActorData, signalHandler)

        assertThat(ram.getDataPort().getOutgoingSignal() as Word, `is`(Word.of(BitWidth.BW_8, 99L)))
    }

    /** ---- [RAMCalculatorTests] */

    private fun createRam(clocked: Boolean): RAM {
        val ram = RAM(clocked)
        ram.setAddressWidth(BitWidth.BW_8)
        ram.setDataWidth(BitWidth.BW_8)
        return ram
    }
}