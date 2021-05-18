package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.addressable.Addressable.Companion.DATA_PORT_NAME
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit test for [ROM].
 */
class ROMTest {

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

    private val rom = ROM()
    private val calculator = ROM.CALCULATOR
    private val signalHandler: SignalHandler = mockk(relaxed = true)

    @Test
    fun shouldReadAndWrite() {
        rom.write(0, 99)
        assertEquals(99L, rom.read(0))
    }

    @Test
    fun shouldReadZeroFromUnwrittenAddress() {
        assertEquals(0L, rom.read(1234))
    }

    @Test
    fun shouldReadWhenCS() {
        rom.write(1, 99)
        rom.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 1L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(DATA_PORT_NAME)
        assertEquals(Word.of(BitWidth.BW_8, 99L), dataOutput.getOutgoingSignal() as Word)
    }

    @Test
    fun shouldBeUndefinedWithoutCS() {
        rom.write(1, 99)
        rom.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 0L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(DATA_PORT_NAME)
        assertEquals(Word.undefined(BitWidth.BW_8), dataOutput.getOutgoingSignal() as Word)
    }

    @Test
    fun shouldBeErrorWithUndefinedAddress() {
        rom.write(1, 99)
        rom.getAddressInput().setIncomingSignal(Word.undefined(BitWidth.BW_8), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 1L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(DATA_PORT_NAME)
        assertEquals(Word.error(BitWidth.BW_8), dataOutput.getOutgoingSignal() as Word)
    }

    @Test
    fun shouldGetCurrentAddress() {
        rom.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 16L), signalHandler)
	    rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 1L), signalHandler)
	    calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)
        assertEquals(16, rom.currentAddress)
    }

    @Test
    fun shouldGetCurrentData() {
        rom.write(1, 255)
        rom.getAddressInput().setIncomingSignal(Word.of(BitWidth.BW_8, 1L), signalHandler)
	    rom.getChipSelectInput().setIncomingSignal(Word.of(BitWidth.BW_1, 1L), signalHandler)
	    calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()) as GraphActorData, signalHandler)
        assertEquals(255L, rom.data)
    }
}