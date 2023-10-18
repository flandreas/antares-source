package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.parser.TestcaseTokenType.*
import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.parser.TokenType

enum class TestcaseTokenType(override val id: String) : TokenType {
	DONT_CARE("X"),
	UNDEFINED("Z"),
	CARET("^")
}

class TestcaseLexer(text: String) : BaseLexer(text) {

	companion object {
		private val CARET_TOKEN = Token<Unit>(CARET)
		private val DONT_CARE_TOKEN = Token<Unit>(DONT_CARE)
		private val UNDEFINED_TOKEN = Token<Unit>(UNDEFINED)
	}

	/** Testcases are line-oriented, so newlines are not whitespace.*/
	override fun isWhitespace(state: State): Boolean =
		super.isWhitespace(state) && state.currentChar != '\n'

	override fun nextTokenImpl(state: State): Token<Any> {
		when (state.currentChar!!) {
			'\n' -> return advanceWith(state, EOL_TOKEN)
			'^' -> return advanceWith(state, CARET_TOKEN)
			'X','x' -> return advanceWith(state, DONT_CARE_TOKEN)
			'Z', 'z' -> return advanceWith(state, UNDEFINED_TOKEN)
		}

		return super.nextTokenImpl(state)
	}
}