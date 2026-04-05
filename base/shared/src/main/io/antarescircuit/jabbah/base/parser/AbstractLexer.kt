package io.antarescircuit.jabbah.base.parser

import io.antarescircuit.jabbah.base.dsl.SyntaxError

/**
 * Lexical analyser, also known as scanner or tokenizer.
 *
 * This class is responsible for breaking a sentence apart into [Token]s, one [Token] at a time.
 * Inspects the [Char] at the current position, advances the current position, and
 * returns the [Token] that corresponds with the consumed [Char].
 **
 * @property text the text to be scanned
 */

abstract class AbstractLexer(protected val text: String) {

	/** The [TextLocation] of the start of the [Token] after reading it with [nextToken].*/
	val location: TextLocation get() = state.location

	val row: Int get() = state.rowCounter

	private val peekState = State()

	protected val state = State()

	protected fun isPeeking(state: State): Boolean = state === peekState

	protected abstract fun nextToken(state: State): Token<Any>

	/**
	 * Scans more text and returns the next [Token].
	 * @throws [SyntaxError] if a syntax error was detected
	 */
	fun nextToken(): Token<Any> = nextToken(state)

	fun peekNextToken(): Token<Any> = nextToken(peekState.applyFrom(state))

	/** Returns the next [Char] (if any) without incrementing [State.pos].*/
	protected fun peek(state: State): Char? = peek(state, 1)

	protected fun peek(state: State, count: Int): Char? {
		val peekPos = state.pos + count
		if (peekPos > text.length - 1) {
			return null
		}
		return text[peekPos]
	}

	protected open fun isWhitespace(state: State): Boolean =
		state.currentChar != null && state.currentChar!!.isWhitespace()

	/** Advances until [State.currentChar] is non-whitespace.*/
	protected fun skipWhitespace(state: State) {
		while (isWhitespace(state)) {
			advance(state)
		}
	}

	/** Advances [State.pos] one position and updates [State.currentChar].*/
	protected fun advance(state: State) {
		if (state.currentChar == '\n') {
			state.rowCounter++
			state.columnCounter = 0
		}
		state.columnCounter++
		state.pos++
		state.currentChar = if (state.pos > text.length - 1) null else text[state.pos]
	}

	protected fun advanceWith(state: State, token: Token<Any>): Token<Any> {
		advance(state)
		return token
	}

	protected fun recordLocation(state: State) {
		state.posAtTokenStart = state.pos
		state.rowAtTokenStart = state.rowCounter
		state.columnAtTokenStart = state.columnCounter
	}

	protected inner class State {

		/** An index into [text].*/
		var pos = 0

		/** Contains the [Char] in [text] at position [pos], or `null` if the end of [text] has been reached.*/
		var currentChar: Char? = if (text.isEmpty()) null else text.first()

		/** Counts the processed number of rows (lines) for syntax error location indication.*/
		var rowCounter = 1

		/** Counts the processed number of columns (characters) within [rowCounter] for syntax error location indication.*/
		var columnCounter = 1

		var posAtTokenStart = 0

		var rowAtTokenStart = 1

		var columnAtTokenStart = 1

		/** Returns the captured [TextLocation] at the start of the last read [Token].*/
		val location: TextLocation get() = TextLocation(posAtTokenStart, rowAtTokenStart, columnAtTokenStart)

		fun applyFrom(other: State): State {
			this.pos = other.pos
			this.currentChar = other.currentChar
			this.posAtTokenStart = other.posAtTokenStart
			this.rowCounter = other.rowCounter
			this.columnCounter = other.columnCounter
			return this
		}
	}
}