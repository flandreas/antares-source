package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.BaseTokenType.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.parser.AbstractLexer
import ch.scorpion.jabbah.base.parser.Token

/**
 * A base lexer implementation for scanning expressions or scripting languages.
 * Supports scanning whitespace, comments, literals, and double-quoted strings.
 * Line comments start with // and eliminate everything to the next newline character.
 *
 * @param text the text to be scanned
 */
open class BaseLexer(text: String) : AbstractLexer(text) {

	companion object {

		private val LOG by logger(BaseLexer::class)

		private val RESERVED_KEYWORDS = mapOf<String, Token<String>>()

		// Singleton instances of value-less [Token]s
		val EOF_TOKEN = Token<Unit>(EOF)
		val EOL_TOKEN = Token<Unit>(EOL)
		private val DOUBLE_QUOTE_TOKEN = Token<Unit>(DOUBLE_QUOTE)

		// Factory methods for [Token]s with values
		fun idToken(value: String) = Token(ID, value)
		fun literalToken(value: Any) = Token(LITERAL, value)
	}

	override fun nextToken(state: State): Token<Any> {
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

			recordLocation(state)

			val token = nextTokenImpl(state)
			if (LOG.isTraceEnabled() && state === this.state) {
				LOG.trace(token.toString())
			}

			return token
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

	/** Determines whether the current character is the begin of a comment.*/
	private fun isComment(state: State): Boolean = state.currentChar == '/' && peek(state) == '/'

	private fun skipComment(state: State) {
		while (state.currentChar != null && state.currentChar != '\n') {
			advance(state)
		}
		advance(state)
	}

	protected open fun isLiteral(state: State): Boolean = isNumber(state) || isString(state)

	protected open fun literal(state: State): Token<Any> {
		return when {
			isNumber(state) -> number(state)
			isString(state) -> literalToken(string(state))
			else -> throw SyntaxError(state.location, Translations.getString("base.dsl.unknownLiteral.msg"))
		}
	}

	protected open fun isNumber(state: State): Boolean = state.currentChar!!.isDigit()

	/** Returns a multi-digit [Long] of [Float] */
	protected open fun number(state: State): Token<Any> {
		val result = StringBuilder()
		while (state.currentChar != null && state.currentChar!!.isDigit()) {
			result.append(state.currentChar)
			advance(state)
		}
		if (state.currentChar == '.') {
			result.append(state.currentChar)
			advance(state)
			while (state.currentChar != null && state.currentChar!!.isDigit()) {
				result.append(state.currentChar)
				advance(state)
			}
			return literalToken(result.toString().toFloat())
		} else {
			return literalToken(result.toString().toLong())
		}
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

	protected open fun id(state: State): Token<String> {
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
}