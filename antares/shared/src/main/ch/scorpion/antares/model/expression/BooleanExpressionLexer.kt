package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.dsl.Token
import ch.scorpion.jabbah.base.dsl.TokenType

class BooleanExpressionLexer(text: String): BaseLexer(text) {

	companion object {
		// General
		private val ASSIGN_TOKEN = Token<Unit>(TokenType.ASSIGN)
		private val LPAREN_TOKEN = Token<Unit>(TokenType.LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(TokenType.RPAREN)

		// Arithmetic notation
		private val MULTIPLY_TOKEN = Token<Unit>(TokenType.MULTIPLY)
		private val PLUS_TOKEN = Token<Unit>(TokenType.PLUS)
		private val SINGLE_QUOTE_TOKEN = Token<Unit>(TokenType.SINGLE_QUOTE)

		// Logic notation
		private val LOGIC_AND_TOKEN = Token<Unit>(TokenType.LOGIC_AND)
		private val LOGIC_OR_TOKEN = Token<Unit>(TokenType.LOGIC_OR)
		private val LOGIC_NOT_TOKEN = Token<Unit>(TokenType.LOGIC_NOT)

		// Programming notation
		private val PROGRAMMING_AND_TOKEN = Token<Unit>(TokenType.PROGRAMMING_AND)
		private val PROGRAMMING_OR_TOKEN = Token<Unit>(TokenType.PROGRAMMING_OR)
		private val PROGRAMMING_NOT_TOKEN = Token<Unit>(TokenType.PROGRAMMING_NOT)

		// Verbose notation
		private val VERBOSE_AND_TOKEN = Token<String>(TokenType.AND)
		private val VERBOSE_OR_TOKEN = Token<String>(TokenType.OR)
		private val VERBOSE_NOT_TOKEN = Token<String>(TokenType.NOT)

		private val RESERVED_KEYWORDS = mapOf(
			"AND" to VERBOSE_AND_TOKEN,
			"OR" to VERBOSE_OR_TOKEN,
			"NOT" to VERBOSE_NOT_TOKEN,
			"and" to VERBOSE_AND_TOKEN,
			"or" to VERBOSE_OR_TOKEN,
			"not" to VERBOSE_NOT_TOKEN
		)
	}

	override fun getReservedKeyword(name: String): Token<String>? =
		RESERVED_KEYWORDS[name]

	override fun nextTokenImpl(state: State): Token<Any> {
		if (isProgrammingAnd(state)) {
			return programmingAnd(state)
		}
		if (isProgrammingOr(state)) {
			return programmingOr(state)
		}
		when (state.currentChar!!) {
			// General
			'=' -> return advanceWith(state, ASSIGN_TOKEN)
			'(' -> return advanceWith(state, LPAREN_TOKEN)
			')' -> return advanceWith(state, RPAREN_TOKEN)

			// Arithmetic notation
			'*' -> return advanceWith(state, MULTIPLY_TOKEN)
			'+' -> return advanceWith(state, PLUS_TOKEN)
			'\'' -> return advanceWith(state, SINGLE_QUOTE_TOKEN)

			// Logic notation
			'∧' -> return advanceWith(state, LOGIC_AND_TOKEN)
			'∨' -> return advanceWith(state, LOGIC_OR_TOKEN)
			'¬' -> return advanceWith(state, LOGIC_NOT_TOKEN)

			// Programming notation
			'!' -> return advanceWith(state, PROGRAMMING_NOT_TOKEN)
		}

		return super.nextTokenImpl(state)
	}

	override fun literal(state: State): Token<Any> = literalToken(booleanLiteral(state))

	private fun isProgrammingAnd(state: State): Boolean = state.currentChar == '&' && peek(state) == '&'
	private fun isProgrammingOr(state: State): Boolean = state.currentChar == '|' && peek(state) == '|'

	private fun programmingAnd(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return PROGRAMMING_AND_TOKEN
	}

	private fun programmingOr(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return PROGRAMMING_OR_TOKEN
	}

	private fun booleanLiteral(state: State): Boolean =
		when (long(state)) {
			0L -> false
			1L -> true
			else -> throw SyntaxError(
				state.location,
				Translations.getString("base.dsl.illegalNumber.msg", "${state.currentChar}"))
		}
}