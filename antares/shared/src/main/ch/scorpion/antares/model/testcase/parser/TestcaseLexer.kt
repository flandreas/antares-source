package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.dsl.DslTokenType
import ch.scorpion.jabbah.base.parser.Token

class TestcaseLexer(text: String) : BaseLexer(text) {

	companion object {
		private val EOL_TOKEN = Token<Unit>(DslTokenType.EOL)

		fun valueToken(value: ULong) = Token(DslTokenType.LITERAL, value)
	}

	/** Testcases are line-oriented, so newlines are not whitespace.*/
	override fun isWhitespace(state: State): Boolean =
		super.isWhitespace(state) && state.currentChar != '\n'

	override fun nextTokenImpl(state: State): Token<Any> {
		when (state.currentChar!!) {
			'\n' -> return advanceWith(state, EOL_TOKEN)
		}

		return super.nextTokenImpl(state)
	}
}