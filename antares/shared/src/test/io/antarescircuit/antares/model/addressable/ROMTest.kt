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
import kotlin.test.assertNull

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

	@Test
	fun shouldRestoreSparseContentsCommentsAndDisassemblyAfterSimulation() {
		rom.addressWidth = BitWidth.BW_32
		rom.disassemblerConfig = "11=Original\n22=Changed"
		rom.setDataAt(3, 0x11UL, null)
		rom.setCommentAt(3, "Original comment", null)
		rom.setCommentAt(Int.MAX_VALUE, "Comment without data", null)
		val initialContents = rom.memory.getNonZeroCells().asSequence().toList()

		rom.executionInitialize(signalHandler)
		rom.setDataAt(3, 0x22UL, signalHandler)
		rom.setCommentAt(3, "Changed comment", signalHandler)
		rom.setDataAt(Int.MAX_VALUE, 99UL, signalHandler)
		rom.setDataAt(4, 42UL, signalHandler)
		assertEquals("Changed", rom.disassemblyAt(3))

		rom.executionStopped(signalHandler)

		assertEquals(initialContents, rom.memory.getNonZeroCells().asSequence().toList())
		assertEquals("Original", rom.disassemblyAt(3))
		assertEquals("", rom.disassemblyAt(4))
		assertNull(rom.commentAt(4))
	}

	@Test
	fun shouldTakeFreshSnapshotForEachSimulationAndIgnoreRepeatedStop() {
		rom.write(3, 17UL)
		rom.executionInitialize(signalHandler)
		rom.setDataAt(3, 42UL, signalHandler)
		rom.executionStopped(signalHandler)
		assertEquals(17UL, rom.read(3))

		rom.write(3, 23UL)
		rom.executionStopped(signalHandler)
		assertEquals(23UL, rom.read(3))

		rom.executionInitialize(signalHandler)
		rom.clear()
		rom.executionStopped(signalHandler)
		assertEquals(23UL, rom.read(3))
	}
}
