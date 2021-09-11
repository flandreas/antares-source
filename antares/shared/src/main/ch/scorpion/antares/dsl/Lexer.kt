package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/** Identifies a location in the code to identify error locations.*/
data class CodeLocation(val row: Int, val column: Int) {
	override fun toString(): String = "$row:$column"
}

/**
 * Lexical analyser, also known as scanner or tokenizer.
 *
 * This class is responsible for breaking a sentence apart into [Token]s, one [Token] at a time.
 * Inspects the [Char] at the current position, advances the current position, and
 * returns the [Token] that corresponds with the consumed [Char].
 *
 * Line comments start with // and eliminate everything to the next newline character.
 *
 * @property text the text to be scanned
 */
class Lexer(private val text: String) {

	companion object {

		// Singleton instances of value-less [Token]s
		private val PLUS_TOKEN = Token<Unit>(PLUS)
		private val MINUS_TOKEN = Token<Unit>(MINUS)
		private val MULTIPLY_TOKEN = Token<Unit>(MULTIPLY)
		private val DIVIDE_TOKEN = Token<Unit>(DIVIDE)
		private val LPAREN_TOKEN = Token<Unit>(LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(RPAREN)
		private val EOF_TOKEN = Token<Unit>(EOF)
		private val EOL_TOKEN = Token<Unit>(EOL)
		private val ASSIGN_TOKEN = Token<Unit>(ASSIGN)
		private val LCURLEY_TOKEN = Token<Unit>(LCURLEY)
		private val RCURLEY_TOKEN = Token<Unit>(RCURLEY)
		private val VAR_TOKEN = Token<String>(VAR)
		private val EQUAL_TOKEN = Token<Unit>(EQUAL)
		private val IF_TOKEN = Token<String>(IF)

		private val RESERVED_KEYWORDS = mapOf(
			"var" to VAR_TOKEN,
			"if" to IF_TOKEN
		)

		// Factory methods for [Token]s with values
		private fun integer(value: Int) = Token(INTEGER, value)
		private fun id(value: String) = Token(ID, value)

		fun getReservedWords(): Collection<String> = RESERVED_KEYWORDS.keys
	}

	private inner class State {
		/** An index into [text].*/
		var pos = 0

		/** Contains the [Char] in [text] at position [pos], or `null` if the end of [text] has been reached.*/
		var currentChar: Char? = if (text.isEmpty()) null else text.first()

		/** Counts the processed number of rows (lines) for syntax error location indication.*/
		var rowCounter = 1

		/** Counts the processed number of columns (characters) within [rowCounter] for syntax error location indication.*/
		var columnCounter = 0

		var rowAtTokenStart = 1

		var columnAtTokenStart = 0

		val location: CodeLocation get() = CodeLocation(rowAtTokenStart,columnAtTokenStart + 1)

		fun applyFrom(other: State): State {
			this.pos = other.pos
			this.currentChar = other.currentChar
			this.rowCounter = other.rowCounter
			this.columnCounter = other.columnCounter
			return this
		}
	}

	private val state = State()

	private val peekState = State()

	val location: CodeLocation get() = state.location

	/**
	 * Scans more text and returns the next [Token].
	 * @throws [SyntaxError] if a syntax error was detected
	 */
	fun nextToken(): Token<Any> = nextToken(state)

	fun peekNextToken(): Token<Any> = nextToken(peekState.applyFrom(state))

	private fun nextToken(state: State): Token<Any> {
		state.rowAtTokenStart = state.rowCounter
		state.columnAtTokenStart = state.columnAtTokenStart

		while (state.currentChar != null) {

			if (isWhitespace(state)) {
				skipWhitespace(state)
				continue
			}

			if (isComment(state)) {
				advance(state)
				advance(state)
				skipComment(state)
				continue
			}

			if (state.currentChar!!.isDigit()) {
				return number(state)
			}

			if (state.currentChar!!.isLetter()) {
				return id(state)
			}

			if (isEqual(state)) {
				return equal(state)
			}

			when (state.currentChar!!) {
				'/' -> return advanceWith(state, DIVIDE_TOKEN)
				'+' -> return advanceWith(state, PLUS_TOKEN)
				'-' -> return advanceWith(state, MINUS_TOKEN)
				'*' -> return advanceWith(state, MULTIPLY_TOKEN)
				'(' -> return advanceWith(state, LPAREN_TOKEN)
				')' -> return advanceWith(state, RPAREN_TOKEN)
				'\n' -> return advanceWith(state, EOL_TOKEN)
				'=' -> return advanceWith(state, ASSIGN_TOKEN)
				'{' -> return advanceWith(state, LCURLEY_TOKEN)
				'}' -> return advanceWith(state, RCURLEY_TOKEN)
			}

			throw SyntaxError(state.location, "Invalid character '${state.currentChar}'")
		}
		return EOF_TOKEN
	}

	/** Determines whether the current character is the begin of a comment.*/
	private fun isComment(state: State): Boolean =
		state.currentChar == '/' && peek(state) == '/'

	private fun isEqual(state: State): Boolean =
		state.currentChar == '=' && peek(state) == '='

	private fun skipComment(state: State) {
		while (state.currentChar != null && state.currentChar != '\n') {
			advance(state)
		}
		advance(state)
	}

	/** Returns an [Int] consumed from the input. Subclasses might override this method to return other number types, such as [Double]s.*/
	private fun number(state: State): Token<Any> = Companion.integer(integer(state))

	/** Returns the next [Char] (if any) without incrementing [State.pos].*/
	private fun peek(state: State): Char? {
		val peekPos = state.pos + 1
		if (peekPos > text.length - 1) {
			return null
		}
		return text[peekPos]
	}

	/** Advances [State.pos] one position and updates [State.currentChar].*/
	private fun advance(state: State) {
		if (state.currentChar == '\n') {
			state.rowCounter++
			state.columnCounter = 0
		}
		state.columnCounter++
		state.pos++
		state.currentChar = if (state.pos > text.length - 1) null else text[state.pos]
	}

	private fun advanceWith(state: State, token: Token<Any>): Token<Any> {
		advance(state)
		return token
	}

	private fun isWhitespace(state: State): Boolean =
		state.currentChar != null && state.currentChar!!.isWhitespace() && state.currentChar != '\n'

	/** Advances until non-whitespace [State.currentChar] is non-whitespace.*/
	private fun skipWhitespace(state: State) {
		while (isWhitespace(state)) {
			advance(state)
		}
	}

	/** Returns a multi-digit [Int] consumed from the input text.*/
	private fun integer(state: State): Int {
		val result = StringBuilder()
		while (state.currentChar != null && state.currentChar!!.isDigit()) {
			result.append(state.currentChar!!)
			advance(state)
		}
		try {
			return result.toString().toInt()
		} catch (e: NumberFormatException) {
			throw SyntaxError(state.location, "Illegal integer '${result}'")
		}
	}

	private fun id(state: State): Token<String> {
		val result = StringBuilder()
		while (state.currentChar != null && state.currentChar!!.isLetterOrDigit()) {
			result.append(state.currentChar)
			advance(state)
		}
		val name = result.toString()
		return RESERVED_KEYWORDS.getOrElse(name) { Companion.id(name) }
	}

	private fun equal(state: State): Token<Any> {
		advance(state)
		advance(state)
		return EQUAL_TOKEN
	}
}