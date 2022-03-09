package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations

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
open class BaseLexer(protected val text: String) {

	companion object {

		private val RESERVED_KEYWORDS = mapOf<String, Token<String>>()

		// Singleton instances of value-less [Token]s
		private val EOF_TOKEN = Token<Unit>(TokenType.EOF)
		private val DOUBLE_QUOTE_TOKEN = Token<Unit>(TokenType.DOUBLE_QUOTE)

		// Factory methods for [Token]s with values
		fun idToken(value: String) = Token(TokenType.ID, value)
		fun literalToken(value: Any) = Token(TokenType.LITERAL, value)
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

	protected open fun nextToken(state: State): Token<Any> {
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

			return nextTokenImpl(state)
		}

		return EOF_TOKEN
	}

	protected open fun nextTokenImpl(state: State): Token<Any> {
		if (isLiteral(state)) {
			return literal(state)
		}

		if (state.currentChar!! == '\'') {
			return quotedId(state)
		}

		if (state.currentChar!!.isLetter()) {
			return id(state)
		}

		when (state.currentChar!!) {
			'"' -> return advanceWith(state, DOUBLE_QUOTE_TOKEN)
		}

		throw SyntaxError(state.location, Translations.getString("base.dsl.invalidCharacter.msg", "${state.currentChar}"))
	}

	protected open fun getReservedKeyword(name: String): Token<String>? = RESERVED_KEYWORDS[name]

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

	/** Returns the next [Char] (if any) without incrementing [State.pos].*/
	protected fun peek(state: State): Char? = peek(state, 1)

	protected fun peek(state: State, count: Int): Char? {
		val peekPos = state.pos + count
		if (peekPos > text.length - 1) {
			return null
		}
		return text[peekPos]
	}

	private fun isWhitespace(state: State): Boolean =
		state.currentChar != null && state.currentChar!!.isWhitespace()

	/** Advances until non-whitespace [State.currentChar] is non-whitespace.*/
	private fun skipWhitespace(state: State) {
		while (isWhitespace(state)) {
			advance(state)
		}
	}

	/** Determines whether the current character is the begin of a comment.*/
	private fun isComment(state: State): Boolean = state.currentChar == '/' && peek(state) == '/'

	private fun skipComment(state: State) {
		while (state.currentChar != null && state.currentChar != '\n') {
			advance(state)
		}
		advance(state)
	}

	protected open fun isLiteral(state: State): Boolean = isNumber(state) || isString(state)

	protected open fun literal(state: State): Token<Any> = number(state)

	protected open fun isNumber(state: State): Boolean = isLong(state)

	protected open fun number(state: State): Token<Any> =
		when {
			isLong(state) -> literalToken(long(state))
			isString(state) -> literalToken(string(state))
			else -> throw SyntaxError(state.location, Translations.getString("base.dsl.unknownLiteral.msg"))
		}

	private fun isLong(state: State): Boolean = state.currentChar!!.isDigit()

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
		return getReservedKeyword(name) ?: idToken(name)
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

	private fun isString(state: State): Boolean = state.currentChar!! == '\"'

	private fun string(state: State): String {
		val result = StringBuilder()
		advance(state)
		while (state.currentChar != null && state.currentChar != '\"') {
			result.append(state.currentChar)
			advance(state)
		}
		if (state.currentChar == '\"') {
			advance(state)
		} else {
			throw SyntaxError(state.location, Translations.getString("base.dsl.expectedDoubleQuote.msg"))
		}
		return result.toString()
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
}