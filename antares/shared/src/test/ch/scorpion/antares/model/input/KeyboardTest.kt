package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
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
		keyboard.enter(char.code.toByte(), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(null))
	}

	@Test
	fun shouldAddByte() {
		enter('A')

		assertEquals(1, keyboard.bufferItemsCount)
		assertEquals('A'.code.toByte(), keyboard.getBytes().next())
	}

	@Test
	fun shouldNotExceedMaxBufferSize() {
		keyboard.bufferSize = 1
		enter('A')
		enter('B')

		assertEquals(1, keyboard.bufferItemsCount)
		assertEquals('A'.code.toByte(), keyboard.getBytes().next())
	}

	@Test
	fun shouldClear() {
		enter('A')
		keyboard.clearInput.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.clearInput))

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 0), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(false), keyboard.availableData.getOutgoingSignal())
		assertEquals(0, keyboard.bufferItemsCount)
	}

	@Test
	fun shouldUpdateAvailableOutput() {
		assertEquals(DigitalSignalFactory.of(false), keyboard.availableData.getOutgoingSignal())
		enter('A')
		assertEquals(DigitalSignalFactory.of(true), keyboard.availableData.getOutgoingSignal())
	}

	@Test
	fun shouldOutputImmediately() {
		enter('A')
		keyboard.readEnableInput.setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(null))

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 'A'.code.toLong()), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(true), keyboard.availableData.getOutgoingSignal())
		assertEquals(1, keyboard.bufferItemsCount)
	}

	@Test
	fun shouldConsume() {
		enter('A')
		keyboard.readEnableInput.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.readEnableInput))
		keyboard.clockInput.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.clockInput))

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 0), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(false), keyboard.availableData.getOutgoingSignal())
		assertEquals(0, keyboard.bufferItemsCount)
	}

	@Test
	fun shouldNotConsumeIfReadNotEnabled() {
		enter('A')
		keyboard.clockInput.setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		keyboard.act(signalHandler, keyboard.createActorData(keyboard.clockInput))

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 'A'.code.toLong()), keyboard.dataOutput.getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(true), keyboard.availableData.getOutgoingSignal())
		assertEquals(1, keyboard.bufferItemsCount)
	}
}