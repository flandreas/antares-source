package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.TokenType.*

/** Identifies a location in the code to identify error locations.*/
data class CodeLocation(val pos: Int, val row: Int, val column: Int) {
	companion object {
		val UNDEFINED = CodeLocation(0, 0, 0)
	}
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
open class Lexer(private val text: String) {

	companion object {

		// Singleton instances of value-less [Token]s
		private val PLUS_TOKEN = Token<Unit>(PLUS)
		private val MINUS_TOKEN = Token<Unit>(MINUS)
		private val MULTIPLY_TOKEN = Token<Unit>(MULTIPLY)
		private val DIVIDE_TOKEN = Token<Unit>(DIVIDE)
		private val LPAREN_TOKEN = Token<Unit>(LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(RPAREN)
		private val EOF_TOKEN = Token<Unit>(EOF)
		private val ASSIGN_TOKEN = Token<Unit>(ASSIGN)
		private val LCURLEY_TOKEN = Token<Unit>(LCURLEY)
		private val RCURLEY_TOKEN = Token<Unit>(RCURLEY)
		private val VAR_TOKEN = Token<String>(VAR)
		private val STORE_TOKEN = Token<String>(STORE)
		private val EQUAL_TOKEN = Token<Unit>(EQUAL)
		private val DIFF_TOKEN = Token<Unit>(DIFF)
		private val IF_TOKEN = Token<String>(IF)
		private val ELSE_TOKEN = Token<String>(ELSE)
		private val AND_TOKEN = Token<String>(AND)
		private val OR_TOKEN = Token<String>(OR)
		private val NOT_TOKEN = Token<String>(NOT)
		private val GREATER_TOKEN = Token<Unit>(GREATER)
		private val GREATER_EQUAL_TOKEN = Token<Unit>(GREATER_EQUAL)
		private val SMALLER_TOKEN = Token<Unit>(SMALLER)
		private val SMALLER_EQUAL_TOKEN = Token<Unit>(SMALLER_EQUAL)
		private val SHIFT_LEFT_TOKEN = Token<Unit>(SHIFT_LEFT)
		private val SHIFT_RIGHT_TOKEN = Token<Unit>(SHIFT_RIGHT)
		private val MOD_TOKEN = Token<String>(MOD)
		private val WHEN_TOKEN = Token<String>(WHEN)
		private val COLON_TOKEN = Token<String>(COLON)
		private val FOR_TOKEN = Token<String>(FOR)
		private val IN_TOKEN = Token<String>(IN)
		private val TO_TOKEN = Token<String>(TO)
		private val CARET_TOKEN = Token<String>(CARET)
		private val LEFT_BRACKET_TOKEN = Token<Unit>(LEFT_BRACKET)
		private val RIGHT_BRACKET_TOKEN = Token<Unit>(RIGHT_BRACKET)
		private val QUESTION_MARK_TOKEN = Token<Unit>(QUESTION_MARK)
		private val RETURN_TOKEN = Token<String>(RETURN)
		private val INIT_TOKEN = Token<String>(INIT)
		private val AT_TOKEN = Token<Unit>(AT)
		private val HASH_TOKEN = Token<Unit>(HASH)
		private val DOT_TOKEN = Token<Unit>(DOT)
		private val COMMA_TOKEN = Token<Unit>(COMMA)
		private val DOUBLE_QUOTE_TOKEN = Token<Unit>(DOUBLE_QUOTE)

		val RESERVED_KEYWORDS = mapOf(
			"var" to VAR_TOKEN,
			"store" to STORE_TOKEN,
			"if" to IF_TOKEN,
			"else" to ELSE_TOKEN,
			"and" to AND_TOKEN,
			"or" to OR_TOKEN,
			"not" to NOT_TOKEN,
			"when" to WHEN_TOKEN,
			"for" to FOR_TOKEN,
			"in" to IN_TOKEN,
			"to" to TO_TOKEN,
			"return" to RETURN_TOKEN,
			"init" to INIT_TOKEN
		)

		fun getReservedWords(): Collection<String> = RESERVED_KEYWORDS.keys
	}

	protected inner class State {
		/** An index into [text].*/
		var pos = 0

		/** Contains the [Char] in [text] at position [pos], or `null` if the end of [text] has been reached.*/
		var currentChar: Char? = if (text.isEmpty()) null else text.first()

		/** Counts the processed number of rows (lines) for syntax error location indication.*/
		var rowCounter = 1

		/** Counts the processed number of columns (characters) within [rowCounter] for syntax error location indication.*/
		var columnCounter = 0

		var posAtTokenStart = 0

		var rowAtTokenStart = 1

		var columnAtTokenStart = 0

		val location: CodeLocation get() = CodeLocation(posAtTokenStart, rowAtTokenStart,columnAtTokenStart + 1)

		fun applyFrom(other: State): State {
			this.pos = other.pos
			this.currentChar = other.currentChar
			this.posAtTokenStart = other.posAtTokenStart
			this.rowCounter = other.rowCounter
			this.columnCounter = other.columnCounter
			return this
		}
	}

	private val state = State()

	private val peekState = State()

	val location: CodeLocation get() = state.location

	// Factory methods for [Token]s with values
	private fun idToken(value: String) = Token(ID, value)
	protected fun literalToken(value: Any) = Token(LITERAL, value)

	/**
	 * Scans more text and returns the next [Token].
	 * @throws [SyntaxError] if a syntax error was detected
	 */
	fun nextToken(): Token<Any> = nextToken(state)

	fun peekNextToken(): Token<Any> = nextToken(peekState.applyFrom(state))

	private fun nextToken(state: State): Token<Any> {
		state.posAtTokenStart = state.pos
		state.rowAtTokenStart = state.rowCounter
		state.columnAtTokenStart = state.columnCounter

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

			if (isLiteral(state)) {
				return literal(state)
			}

			if (state.currentChar!! == '\'') {
				return quotedId(state)
			}

			if (state.currentChar!!.isLetter()) {
				return id(state)
			}

			if (isEqual(state)) {
				return equal(state)
			}

			if (isDifferent(state)) {
				return diff(state)
			}

			if (isSmallerEqual(state)) {
				return smallerEqual(state)
			}

			if (isGreaterEqual(state)) {
				return greaterEqual(state)
			}

			if (isShiftLeft(state)) {
				return shiftLeft(state)
			}

			if (isShiftRight(state)) {
				return shiftRight(state)
			}

			when (state.currentChar!!) {
				'/' -> return advanceWith(state, DIVIDE_TOKEN)
				'+' -> return advanceWith(state, PLUS_TOKEN)
				'-' -> return advanceWith(state, MINUS_TOKEN)
				'*' -> return advanceWith(state, MULTIPLY_TOKEN)
				'(' -> return advanceWith(state, LPAREN_TOKEN)
				')' -> return advanceWith(state, RPAREN_TOKEN)
				'=' -> return advanceWith(state, ASSIGN_TOKEN)
				'{' -> return advanceWith(state, LCURLEY_TOKEN)
				'}' -> return advanceWith(state, RCURLEY_TOKEN)
				'<' -> return advanceWith(state, SMALLER_TOKEN)
				'>' -> return advanceWith(state, GREATER_TOKEN)
				'%' -> return advanceWith(state, MOD_TOKEN)
				':' -> return advanceWith(state, COLON_TOKEN)
				'^' -> return advanceWith(state, CARET_TOKEN)
				'[' -> return advanceWith(state, LEFT_BRACKET_TOKEN)
				']' -> return advanceWith(state, RIGHT_BRACKET_TOKEN)
				'?' -> return advanceWith(state, QUESTION_MARK_TOKEN)
				'@' -> return advanceWith(state, AT_TOKEN)
				'#' -> return advanceWith(state, HASH_TOKEN)
				'.' -> return advanceWith(state, DOT_TOKEN)
				',' -> return advanceWith(state, COMMA_TOKEN)
				'"' -> return advanceWith(state, DOUBLE_QUOTE_TOKEN)
			}

			throw SyntaxError(state.location, Translations.getString("base.dsl.invalidCharacter.msg", "${state.currentChar}"))
		}
		return EOF_TOKEN
	}

	/** Determines whether the current character is the begin of a comment.*/
	private fun isComment(state: State): Boolean = state.currentChar == '/' && peek(state) == '/'

	private fun isEqual(state: State): Boolean = state.currentChar == '=' && peek(state) == '='

	private fun isDifferent(state: State): Boolean = state.currentChar == '!' && peek(state) == '='

	private fun isSmallerEqual(state: State): Boolean = state.currentChar == '<' && peek(state) == '='

	private fun isGreaterEqual(state: State): Boolean = state.currentChar == '>' && peek(state) == '='

	private fun isShiftLeft(state: State): Boolean = state.currentChar == '<' && peek(state) == '<'

	private fun isShiftRight(state: State): Boolean = state.currentChar == '>' && peek(state) == '>'

	private fun isLong(state: State): Boolean = state.currentChar!!.isDigit()

	protected open fun isNumber(state: State): Boolean = isLong(state)

	protected open fun isLiteral(state: State): Boolean = isNumber(state)

	private fun skipComment(state: State) {
		while (state.currentChar != null && state.currentChar != '\n') {
			advance(state)
		}
		advance(state)
	}

	protected open fun literal(state: State): Token<Any> = number(state)

	protected open fun number(state: State): Token<Any> =
		when {
			isLong(state) -> literalToken(long(state))
			else -> throw SyntaxError(state.location, Translations.getString("base.dsl.expectedNumber.msg"))
		}

	/** Returns the next [Char] (if any) without incrementing [State.pos].*/
	protected fun peek(state: State): Char? = peek(state, 1)

	protected fun peek(state: State, count: Int): Char? {
		val peekPos = state.pos + count
		if (peekPos > text.length - 1) {
			return null
		}
		return text[peekPos]
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

	private fun advanceWith(state: State, token: Token<Any>): Token<Any> {
		advance(state)
		return token
	}

	private fun isWhitespace(state: State): Boolean =
		state.currentChar != null && state.currentChar!!.isWhitespace()

	/** Advances until non-whitespace [State.currentChar] is non-whitespace.*/
	private fun skipWhitespace(state: State) {
		while (isWhitespace(state)) {
			advance(state)
		}
	}

	/** Returns a multi-digit [Long] consumed from the input text.*/
	protected fun long(state: State): Long {
		val result = StringBuilder()
		while (state.currentChar != null && state.currentChar!!.isDigit()) {
			result.append(state.currentChar!!)
			advance(state)
		}
		try {
			return result.toString().toLong()
		} catch (e: NumberFormatException) {
			throw SyntaxError(state.location, Translations.getString("base.dsl.illegalNumber.msg", "${result.ifEmpty { state.currentChar }}"))
		}
	}

	private fun id(state: State): Token<String> {
		val result = StringBuilder()
		while (state.currentChar != null && state.currentChar!!.isLetterOrDigit()) {
			result.append(state.currentChar)
			advance(state)
		}
		val name = result.toString()
		return RESERVED_KEYWORDS.getOrElse(name) { idToken(name) }
	}

	private fun quotedId(state: State): Token<String> {
		val result = StringBuilder()
		advance(state)
		while (state.currentChar != null && state.currentChar != '\'') {
			result.append(state.currentChar)
			advance(state)
		}
		if (state.currentChar == '\'') {
			advance(state)
		} else {
			throw SyntaxError(state.location, Translations.getString("base.dsl.expectedSingleQuote.msg"))
		}
		val id = result.toString()
		if (id.isBlank()) {
			throw SyntaxError(state.location, Translations.getString("base.dsl.emptyIdentifier.msg"))
		}
		return idToken(id)
	}

	private fun equal(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return EQUAL_TOKEN
	}

	private fun diff(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return DIFF_TOKEN
	}

	private fun smallerEqual(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return SMALLER_EQUAL_TOKEN
	}

	private fun greaterEqual(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return GREATER_EQUAL_TOKEN
	}

	private fun shiftLeft(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return SHIFT_LEFT_TOKEN
	}

	private fun shiftRight(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return SHIFT_RIGHT_TOKEN
	}
}