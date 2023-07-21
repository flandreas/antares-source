package ch.scorpion.jabbah.base.richtext

import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.dsl.DslTokenType.*

class RichTextLexer(text: String) : BaseLexer(text) {

	companion object {
		private val LPAREN_TOKEN = Token<Unit>(LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(RPAREN)

		private val OVERLINE_TOKEN = Token<Unit>(PROGRAMMING_NOT)
		private val SUBSCRIPT_TOKEN = Token<Unit>(UNDERSCORE)
		private val SUPERSCRIPT_TOKEN = Token<Unit>(CARET)
	}

	override fun nextTokenImpl(state: State): Token<Any> {
		when (state.currentChar!!) {
			'(' -> return advanceWith(state, LPAREN_TOKEN)
			')' -> return advanceWith(state, RPAREN_TOKEN)
			'!' -> return advanceWith(state, OVERLINE_TOKEN)
			'_' -> return advanceWith(state, SUBSCRIPT_TOKEN)
			'^' -> return advanceWith(state, SUPERSCRIPT_TOKEN)
		}

		return super.nextTokenImpl(state)
	}
}