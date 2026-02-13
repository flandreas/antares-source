package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import ch.scorpion.antares.model.signal.DigitalSignalFactory.undefined
import ch.scorpion.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class RAMSeparateDataPortsCalculatorTest {

    private val calculator = RAMCalculator()
    private val signalHandler: SignalHandler = mock(MockMode.autofill)

    init {
        AntaresTestRule.configure()
    }

    /** ---- Clocked tests */

    @Test
    fun shouldWrite() {
        val ram = createRam(true)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(of(BW_8, 1L), signalHandler)
        ram.getEffectiveDataInput().setIncomingSignal(of(BW_8, 99L), signalHandler)
        ram.getClockInput()!!.setIncomingSignal(of(true), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getClockInput()!!), signalHandler)

        assertEquals(99UL, ram.read(1))
        assertEquals(of(BW_8, 99L), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldNotWriteWhenNotEnabled() {
        val ram = createRam(true)
        ram.getChipSelectInput().setIncomingSignal(of(false), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(of(BW_8, 1L), signalHandler)
        ram.getEffectiveDataInput().setIncomingSignal(of(BW_8, 99L), signalHandler)
        ram.getClockInput()!!.setIncomingSignal(of(true), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getClockInput()), signalHandler)

        assertEquals(0UL, ram.read(1))
        assertEquals(undefined(BW_8), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldUndefineDataWithUndefinedAddressClocked() {
        val ram = createRam(true)
        ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BW_8, Bit.Undefined), signalHandler)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

        assertEquals(undefined(BW_8), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldRead() {
        val ram = createRam(true)
        ram.write(1, 99UL)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(false), signalHandler)
        ram.getClearInput().setIncomingSignal(of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(of(BW_8, 1L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

        assertEquals(of(BW_8, 99L), ram.getDataPort().getOutgoingSignal())
    }

    /** ---- Unclocked tests */

    @Test
    fun shouldWriteUnclocked() {
        val ram = createRam(false)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(of(BW_8, 1L), signalHandler)
        ram.getEffectiveDataInput().setIncomingSignal(of(BW_8, 99L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getEffectiveDataInput()), signalHandler)

        assertEquals(99UL, ram.read(1))
        assertEquals(of(BW_8, 99L), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldNotReadWhenNotEnabled() {
        val ram = createRam(false)
        ram.write(1, 99UL)
        ram.getChipSelectInput().setIncomingSignal(of(false), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(false), signalHandler)
        ram.getClearInput().setIncomingSignal(of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(of(BW_8, 1L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

        assertEquals(undefined(BW_8), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldReadUnclocked() {
        val ram = createRam(false)
        ram.write(1, 99UL)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(false), signalHandler)
        ram.getClearInput().setIncomingSignal(of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(of(BW_8, 1L), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

        assertEquals(of(BW_8, 99L), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldUndefineDataWithUndefinedAddress() {
        val ram = createRam(false)
        ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BW_8, Bit.Undefined), signalHandler)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

        assertEquals(undefined(BW_8), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldNotWriteWithUndefinedData() {
        val ram = createRam(false)
        ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BW_8, Bit.False), signalHandler)
        ram.getDataInput()!!.setIncomingSignal(DigitalSignalFactory.allOf(BW_8, Bit.Undefined), signalHandler)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(true), signalHandler)

        calculator.calculate(ram, ram.createActorData(ram.getDataInput()), signalHandler)

        assertEquals(undefined(BW_8), ram.getDataPort().getOutgoingSignal())
    }

    @Test
    fun shouldOutputDataWhenChangingManuallyUnclocked() {
        val ram = createRam(false)
        ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BW_8, Bit.False), signalHandler)
        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(false), signalHandler)

        ram.setDataAt(0, 255UL, signalHandler)
        calculator.calculate(ram, ram.createActorData(null), signalHandler)

        assertEquals(of(BW_8, 255), ram.getDataPort().getOutgoingSignal())
    }

    /** ---- [RAMSeparateDataPortsCalculatorTest] */

    private fun createRam(clocked: Boolean): RAM {
        val ram = RAM(clocked, true)
        ram.addressWidth = BW_8
        ram.dataWidth = BW_8
        return ram
    }
}