package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.addressable.AddressableVertice.Companion.DATA_PORT_NAME
import io.antarescircuit.antares.model.gate.CurrentUndefinedGateInputBehavior
import io.antarescircuit.antares.model.gate.UndefinedGateInputBehavior
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class ROMTest {

    private val rom: ROM
    private val calculator = ROM.CALCULATOR
    private val signalHandler: SignalHandler = mock(MockMode.autofill)

    init {
        AntaresTestRule.configure()
        rom = ROM()
    }

    @Test
    fun shouldReadAndWrite() {
        rom.write(0, 99UL)
        assertEquals(99UL, rom.read(0))
    }

    @Test
    fun shouldReadZeroFromUnwrittenAddress() {
        assertEquals(0UL, rom.read(1234))
    }

    @Test
    fun shouldReadWhenCS() {
        rom.write(1, 99UL)
        rom.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()), signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(DATA_PORT_NAME)
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 99L), dataOutput.getOutgoingSignal())
    }

    @Test
    fun shouldBeUndefinedWithoutCS() {
        rom.write(1, 99UL)
        rom.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 0L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()), signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(DATA_PORT_NAME)
        assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_8), dataOutput.getOutgoingSignal())
    }

    @Test
    fun shouldUseUndefinedGateInputBehaviorWithUndefinedAddress() {
        CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0
        rom.write(0, 99UL)
        rom.getAddressInput().setIncomingSignal(DigitalSignalFactory.undefined(BitWidth.BW_8), signalHandler)
        rom.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1L), signalHandler)

        calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()), signalHandler)

        val dataOutput = rom.getOutput<DigitalSignal>(DATA_PORT_NAME)
        assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 99L), dataOutput.getOutgoingSignal())
    }

    @Test
    fun shouldGetCurrentAddress() {
        rom.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 16L), signalHandler)
	    rom.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1L), signalHandler)
	    calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()), signalHandler)
        assertEquals(16, rom.currentAddress)
    }

    @Test
    fun shouldGetCurrentData() {
        rom.write(1, 255UL)
        rom.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)
	    rom.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_1, 1L), signalHandler)
	    calculator.calculate(rom, rom.createActorData(rom.getChipSelectInput()), signalHandler)
        assertEquals(255UL, rom.data)
    }
}