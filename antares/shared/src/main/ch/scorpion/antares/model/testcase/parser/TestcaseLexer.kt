package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.testcase.parser.TestcaseTokenType.*
import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.parser.TokenType

enum class TestcaseTokenType(override val id: String) : TokenType {
	DONT_CARE("X"),
	UNDEFINED("Z"),
	CARET("^"),
}

class TestcaseLexer(text: String) : BaseLexer(text) {

	companion object {
		private val CARET_TOKEN = Token<Unit>(CARET)
		private val DONT_CARE_TOKEN = Token<Unit>(DONT_CARE)
		private val UNDEFINED_TOKEN = Token<Unit>(UNDEFINED)
	}

	override fun nextTokenImpl(state: State): Token<Any> {
		if (isHexIdentifier(state)) {
			return literalToken(hexLiteral(state))
		}

		if (isBinaryIdentifier(state)) {
			return literalToken(binaryLiteral(state))
		}

		when (state.currentChar!!) {
			'\n' -> return advanceWith(state, EOL_TOKEN)
			'^' -> return advanceWith(state, CARET_TOKEN)
			'X','x' -> return advanceWith(state, DONT_CARE_TOKEN)
			'Z', 'z' -> return advanceWith(state, UNDEFINED_TOKEN)
		}

		return super.nextTokenImpl(state)
	}

	/** Testcases are line-oriented, so newlines are not whitespace.*/
	override fun isWhitespace(state: State): Boolean =
		super.isWhitespace(state) && state.currentChar != '\n'

	override fun isNumber(state: State): Boolean {
		return isHexIdentifier(state) || super.isNumber(state)
	}

	override fun number(state: State): Token<Any> =
		when {
			isHexIdentifier(state) -> literalToken(hexLiteral(state))
			else -> super.number(state)
		}

	private fun isHexIdentifier(state: State): Boolean = state.currentChar == '0' && peek(state)?.uppercaseChar() == 'X'

	private fun isBinaryIdentifier(state: State): Boolean = state.currentChar == '0' && peek(state)?.uppercaseChar() == 'B'

	private fun hexLiteral(state: State): Long {
		advance(state)
		advance(state)
		val result = StringBuilder()
		while (state.currentChar != null && BitOperation.HEX_CHAR.contains(state.currentChar!!.uppercaseChar())) {
			result.append(state.currentChar!!)
			advance(state)
		}
		return BitOperation.hexToLong(result.toString()).toLong()
	}

	private fun binaryLiteral(state: State): Long {
		advance(state) // 0
		advance(state) // b
		val result = StringBuilder()
		while (state.currentChar != null && BitOperation.BINARY_DIGITS.contains(state.currentChar!!.uppercaseChar())) {
			result.append(state.currentChar!!)
			advance(state)
		}
		return BitOperation.binaryToLong(result.toString()).toLong()
	}
}