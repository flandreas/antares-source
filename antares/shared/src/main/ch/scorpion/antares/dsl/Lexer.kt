package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/** Thrown by [Lexer.nextToken] if a syntax error is detected.*/
class SyntaxError(msg: String) : Throwable(msg)

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

		private fun plus() = PLUS_TOKEN
		private fun minus() = MINUS_TOKEN
		private fun multiply() = MULTIPLY_TOKEN
		private fun divide() = DIVIDE_TOKEN
		private fun lparen() = LPAREN_TOKEN
		private fun rparen() = RPAREN_TOKEN
		private fun eof() = EOF_TOKEN
		private fun eol() = EOL_TOKEN
		private fun assign() = ASSIGN_TOKEN
		private fun lcurley() = LCURLEY_TOKEN
		private fun rcurley() = RCURLEY_TOKEN

		// Factory methods for [Token]s with values
		private fun integer(value: Int) = Token(INTEGER, value)
		private fun id(value: String) = Token(ID, value)
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

		val currentLocation: String get() = "$rowCounter:${columnCounter + 1}"

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

	val currentLocation: String get() = state.currentLocation

	/**
	 * Scans more text and returns the next [Token].
	 * @throws [SyntaxError] if a syntax error was detected
	 */
	fun nextToken(): Token<Any> = nextToken(state)

	fun peekNextToken(): Token<Any> = nextToken(peekState.applyFrom(state))

	private fun nextToken(state: State): Token<Any> {
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

			when (state.currentChar!!) {
				'/' -> {
					advance(state)
					return divide()
				}
				'+' -> {
					advance(state)
					return plus()
				}
				'-' -> {
					advance(state)
					return minus()
				}
				'*' -> {
					advance(state)
					return multiply()
				}
				'(' -> {
					advance(state)
					return lparen()
				}
				')' -> {
					advance(state)
					return rparen()
				}
				'\n' -> {
					advance(state)
					return eol()
				}
				'=' -> {
					advance(state)
					return assign()
				}
				'{' -> {
					advance(state)
					return lcurley()
				}
				'}' -> {
					advance(state)
					return rcurley()
				}
			}

			throw SyntaxError("Invalid character '${state.currentChar}' at ${state.currentLocation}")
		}
		return eof()
	}

	/** Determines whether the current character is the begin of a comment.*/
	private fun isComment(state: State): Boolean =
		state.currentChar == '/' && peek(state) == '/'

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
			throw SyntaxError("Illegal integer '${result}' at ${state.currentLocation}")
		}
	}

	private fun id(state: State): Token<String> {
		val result = StringBuilder()
		while (state.currentChar != null && state.currentChar!!.isLetterOrDigit()) {
			result.append(state.currentChar)
			advance(state)
		}
		return Companion.id(result.toString())
	}
}