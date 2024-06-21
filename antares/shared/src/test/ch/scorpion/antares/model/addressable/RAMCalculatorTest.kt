package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [RAM].
 */
class RAMCalculatorTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val calculator = RAMCalculator()
	private val signalHandler: SignalHandler = mock(MockMode.autofill)

	/** ---- Clocked tests */

	@Test
	fun shouldWrite() {
		val ram = createRam(true)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getClearInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)
		ram.getDataPort().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 99L), signalHandler)
		ram.getClockInput()!!.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getClockInput()!!), signalHandler)

		assertEquals(99UL, ram.read(1))
	}

	@Test
	fun shouldNotWriteWhenNotEnabled() {
		val ram = createRam(true)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getClearInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)
		ram.getDataPort().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 99L), signalHandler)
		ram.getClockInput()!!.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getClockInput()), signalHandler)

		assertEquals(0UL, ram.read(1))
	}

	@Test
	fun shouldUndefineDataWhenWriteEnabled() {
		val ram = createRam(true)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getClearInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getClockInput()), signalHandler)

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal())
	}

	@Test
	fun shouldUndefineDataWithUndefinedAddressClocked() {
		val ram = createRam(true)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BitWidth.BW_8, Bit.Undefined), signalHandler)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal())
	}

	@Test
	fun shouldRead() {
		val ram = createRam(true)
		ram.write(1, 99UL)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getClearInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 99L), ram.getDataPort().getOutgoingSignal())
	}

	/** ---- Unclocked tests */

	@Test
	fun shouldWriteUnclocked() {
		val ram = createRam(false)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getClearInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)
		ram.getDataPort().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 99L), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getDataPort()), signalHandler)

		assertEquals(99UL, ram.read(1))
	}

	@Test
	fun shouldNotReadWhenNotEnabled() {
		val ram = createRam(false)
		ram.write(1, 99UL)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getClearInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal())
	}

	@Test
	fun shouldReadUnclocked() {
		val ram = createRam(false)
		ram.write(1, 99UL)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getClearInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 1L), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 99L), ram.getDataPort().getOutgoingSignal())
	}

	@Test
	fun shouldUndefineDataWithUndefinedAddress() {
		val ram = createRam(false)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BitWidth.BW_8, Bit.Undefined), signalHandler)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getAddressInput()), signalHandler)

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal())
	}

	@Test
	fun shouldNotWriteWithUndefinedData() {
		val ram = createRam(false)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BitWidth.BW_8, Bit.False), signalHandler)
		ram.getDataPort().setIncomingSignal(DigitalSignalFactory.allOf(BitWidth.BW_8, Bit.Undefined), signalHandler)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getDataPort()), signalHandler)

		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal())
	}

	@Test
	fun shouldOutputDataWhenChangingManuallyUnclocked() {
		val ram = createRam(false)
		ram.getAddressInput().setIncomingSignal(DigitalSignalFactory.allOf(BitWidth.BW_8, Bit.False), signalHandler)
		ram.getChipSelectInput().setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)

		ram.setDataAt(0, 255UL, signalHandler)
		calculator.calculate(ram, ram.createActorData(null), signalHandler)

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 255), ram.getDataPort().getOutgoingSignal())
	}

	/** ---- [RAMCalculatorTest] */

	private fun createRam(clocked: Boolean): RAM {
		val ram = RAM(clocked)
		ram.addressWidth = BitWidth.BW_8
		ram.dataWidth = BitWidth.BW_8
		return ram
	}
}