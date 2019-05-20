package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyboardTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val keyboard = Keyboard()
	private val signalHandler: SignalHandler = mockk(relaxed = true)

	private fun enter(char: Char) {
		keyboard.enter(char.toByte(), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(null))
	}

	@Test
	fun shouldAddByte() {
		enter('A')

		assertEquals(1, keyboard.bufferItemsCount)
		assertEquals('A'.toByte(), keyboard.getBytes().next())
	}

	@Test
	fun shouldNotExceedMaxBufferSize() {
		keyboard.bufferSize = 1
		enter('A')
		enter('B')

		assertEquals(1, keyboard.bufferItemsCount)
		assertEquals('A'.toByte(), keyboard.getBytes().next())
	}

	@Test
	fun shouldClear() {
		enter('A')
		keyboard.clearInput.setIncomingSignal(Word.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.clearInput))

		assertEquals(Word.of(BitWidth.BW_8, 0), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(Word.of(false), keyboard.availableData.getOutgoingSignal())
		assertEquals(0, keyboard.bufferItemsCount)
	}

	@Test
	fun shouldUpdateAvailableOutput() {
		assertEquals(Word.of(false), keyboard.availableData.getOutgoingSignal())
		enter('A')
		assertEquals(Word.of(true), keyboard.availableData.getOutgoingSignal())
	}

	@Test
	fun shouldOutputImmediately() {
		enter('A')
		keyboard.readEnableInput.setIncomingSignal(Word.of(false), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(null))

		assertEquals(Word.of(BitWidth.BW_8, 'A'.toLong()), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(Word.of(true), keyboard.availableData.getOutgoingSignal())
		assertEquals(1, keyboard.bufferItemsCount)
	}

	@Test
	fun shouldConsume() {
		enter('A')
		keyboard.readEnableInput.setIncomingSignal(Word.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.readEnableInput))
		keyboard.clockInput.setIncomingSignal(Word.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.clockInput))

		assertEquals(Word.of(BitWidth.BW_8, 0), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(Word.of(false), keyboard.availableData.getOutgoingSignal())
		assertEquals(0, keyboard.bufferItemsCount)
	}

	@Test
	fun shouldNotConsumeIfReadNotEnabled() {
		enter('A')
		keyboard.clockInput.setIncomingSignal(Word.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.clockInput))

		assertEquals(Word.of(BitWidth.BW_8, 'A'.toLong()), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(Word.of(true), keyboard.availableData.getOutgoingSignal())
		assertEquals(1, keyboard.bufferItemsCount)
	}
}