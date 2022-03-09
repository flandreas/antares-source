package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.dsl.Token
import ch.scorpion.jabbah.base.dsl.TokenType

class BooleanExpressionLexer(text: String): BaseLexer(text) {

	companion object {
		private val ASSIGN_TOKEN = Token<Unit>(TokenType.ASSIGN)
		private val MULTIPLY_TOKEN = Token<Unit>(TokenType.MULTIPLY)
		private val PLUS_TOKEN = Token<Unit>(TokenType.PLUS)
		private val SINGLE_QUOTE_TOKEN = Token<Unit>(TokenType.SINGLE_QUOTE)
		private val LPAREN_TOKEN = Token<Unit>(TokenType.LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(TokenType.RPAREN)
	}

	override fun nextTokenImpl(state: State): Token<Any> {
		when (state.currentChar!!) {
			'=' -> return advanceWith(state, ASSIGN_TOKEN)
			'*' -> return advanceWith(state, MULTIPLY_TOKEN)
			'+' -> return advanceWith(state, PLUS_TOKEN)
			'\'' -> return advanceWith(state, SINGLE_QUOTE_TOKEN)
			'(' -> return advanceWith(state, LPAREN_TOKEN)
			')' -> return advanceWith(state, RPAREN_TOKEN)
		}

		return super.nextTokenImpl(state)
	}
}