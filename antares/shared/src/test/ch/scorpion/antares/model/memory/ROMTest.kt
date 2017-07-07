package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import com.nhaarman.mockito_kotlin.mock
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit test for [ROM].
 */
class ROMTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    private val rom = ROM()
    private val calculator = ROM.CALCULATOR
    private val signalHandler: SignalHandler = mock()

    @Test
    fun shouldReadAndWrite() {
        rom.write(0, 99)
        assertThat(rom.read(0), `is`(99L))
    }

    @Test
    fun shouldReadZeroFromUnwrittenAddress() {
        assertThat(rom.read(1234), `is`(0L))
    }

    @Test
    fun shouldReadWhenCS() {
        rom.write(1, 99)
        rom.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 1L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(ROM.DATA_PORT_NAME)
        assertThat(dataOutput.getOutgoingSignal() as Word, `is`(Word.of(BitWidth.BW_8, 99L)))
    }

    @Test
    fun shouldBeUndefinedWithoutCS() {
        rom.write(1, 99)
        rom.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 0L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(ROM.DATA_PORT_NAME)
        assertThat(dataOutput.getOutgoingSignal() as Word, `is`(Word.undefined(BitWidth.BW_8)))
    }

    @Test
    fun shouldBeErrorWithUndefinedAddress() {
        rom.write(1, 99)
        rom.getAddressInput().setIncomingSignal(Word.undefined(BitWidth.BW_8), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 1L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(ROM.DATA_PORT_NAME)
        assertThat(dataOutput.getOutgoingSignal() as Word, `is`(Word.error(BitWidth.BW_8)))
    }
}