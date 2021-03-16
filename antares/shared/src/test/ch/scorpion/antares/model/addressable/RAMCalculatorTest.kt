package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import io.mockk.mockk
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
	private val signalHandler: SignalHandler = mockk(relaxed = true)

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

		assertEquals(99L, ram.read(1))
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

		assertEquals(0L, ram.read(1))
	}

	@Test
	fun shouldUndefineDataWhenWriteEnabled() {
		val ram = createRam(true)
		ram.getChipSelectInput().setIncomingSignal(Word.of(true), signalHandler)
		ram.getWriteInput().setIncomingSignal(Word.of(true), signalHandler)
		ram.getClearInput().setIncomingSignal(Word.of(false), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getClockInput()) as GraphActorData, signalHandler)

		assertEquals(Word.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal() as Word)
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

		assertEquals(Word.of(BitWidth.BW_8, 99L), ram.getDataPort().getOutgoingSignal() as Word)
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

		assertEquals(99L, ram.read(1))
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

		assertEquals(Word.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal() as Word)
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

		assertEquals(Word.of(BitWidth.BW_8, 99L), ram.getDataPort().getOutgoingSignal() as Word)
	}

	@Test
	fun shouldUndefineDataWithUndefinedAddress() {
		val ram = createRam(false)
		ram.getAddressInput().setIncomingSignal(Word.allOf(BitWidth.BW_8, Bit.Undefined), signalHandler)
		ram.getChipSelectInput().setIncomingSignal(Word.of(true), signalHandler)

		calculator.calculate(ram, ram.createActorData(ram.getAddressInput()) as GraphActorData, signalHandler)

		assertEquals(Word.undefined(BitWidth.BW_8), ram.getDataPort().getOutgoingSignal() as Word)
	}

	/** ---- [RAMCalculatorTest] */

	private fun createRam(clocked: Boolean): RAM {
		val ram = RAM(clocked)
		ram.setAddressWidth(BitWidth.BW_8)
		ram.setDataWidth(BitWidth.BW_8)
		return ram
	}
}