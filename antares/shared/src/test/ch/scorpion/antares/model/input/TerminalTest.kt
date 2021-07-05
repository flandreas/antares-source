package ch.scorpion.antares.model.input

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class TerminalTest {

	companion object {
		private const val BACKSPACE = 8.toChar()
		private const val NEWLINE = 10.toChar()
		private const val FORM_FEED = 12.toChar()

		init {
			AntaresTestRule.configure()
		}
	}

	val terminal = Terminal()

	private val signalHandler: SignalHandler = mockk(relaxed = true)

	@Test
	fun shouldAddCharacterIfEnabled() {
		enterEnabled('A')

		assertEquals(1, terminal.displayedRowsCount)
		assertEquals('A', terminal.getRow(0).iterator().next())
	}

	@Test
	fun shouldNoAddCharacterIfNotEnabled() {
		enter('A')

		assertEquals(0, terminal.displayedRowsCount)
	}

	@Test
	fun shouldHandleLinefeed() {
		enterEnabled('A')
		enterEnabled(NEWLINE)
		enterEnabled('B')

		assertEquals(2, terminal.displayedRowsCount)
	}

	@Test
	fun shouldHandleBackspace() {
		enterEnabled('A')
		enterEnabled('B')
		enterEnabled(BACKSPACE)

		assertEquals(1, terminal.displayedRowsCount)
		assertEquals(1, terminal.getRow(0).size)
		assertEquals('A', terminal.getRow(0).last())
	}

	@Test
	fun shouldHandleFormFeed() {
		enterEnabled('A')
		enterEnabled(FORM_FEED)

		assertEquals(0, terminal.displayedRowsCount)
	}

	@Test
	fun shouldBreakAtEndOfLine() {
		val terminal = Terminal(columnsCount = 1)
		enterEnabled(terminal, 'A')
		enterEnabled(terminal, 'B')

		assertEquals(2, terminal.displayedRowsCount)
		assertEquals('A', terminal.getRow(0).last())
		assertEquals('B', terminal.getRow(1).last())
	}

	@Test
	fun shouldScrollRowsUp() {
		val terminal = Terminal(rowsCount = 1)
		enterEnabled(terminal, 'A')
		enterEnabled(NEWLINE)
		enterEnabled(terminal, 'B')

		assertEquals(1, terminal.displayedRowsCount)
		assertEquals('B', terminal.getRow(0).last())
	}

	@Test
	fun shouldClear() {
		enterEnabled('A')
		terminal.clearInput.setIncomingSignal(Word.of(true), signalHandler)
		terminal.act(signalHandler, terminal.createActorData(terminal.clearInput))

		assertEquals(0, terminal.displayedRowsCount)
	}

	@Test
	fun shouldIgnoreNonSupportedCharacter() {
		enterEnabled(0.toChar())

		assertEquals(0, terminal.displayedRowsCount)
	}

	private fun enter(char: Char) {
		enter(terminal, char)
	}

	private fun enterEnabled(char: Char) {
		enterEnabled(terminal, char)
	}

	private fun enterEnabled(term: Terminal, char: Char) {
		term.writeEnableInput.setIncomingSignal(Word.of(true), signalHandler)
		enter(term, char)
	}

	private fun enter(terminal: Terminal, char: Char) {
		terminal.dataInput.setIncomingSignal(Word.of(BitWidth.BW_8, char.code.toLong()), signalHandler)
		terminal.clockInput.setIncomingSignal(Word.of(true), signalHandler)
		terminal.act(signalHandler, terminal.createActorData(terminal.clockInput))
	}
}