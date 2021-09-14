package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.dsl.Lexer
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.dsl.Token

class AntaresLexer(text: String) : Lexer(text) {

	override fun isLiteral(state: State): Boolean =
		super.isLiteral(state) || isUndefinedHexLiteral(state)

	override fun isNumber(state: State): Boolean =
		isHexLiteral(state) || super.isNumber(state)

	override fun literal(state: State): Token<Any> =
		when {
			isUndefinedHexLiteral(state) -> literalToken(undefinedHexLiteral(state))
			isHexLiteral(state) -> literalToken(hexLiteral(state))
			else -> super.literal(state)
		}

	override fun number(state: State): Token<Any> =
		when {
			isHexLiteral(state) -> literalToken(hexLiteral(state))
			else -> super.number(state)
		}

	private fun isHexLiteral(state: State): Boolean =
		state.currentChar == '0' && peek(state) == 'x'

	private fun isUndefinedHexLiteral(state: State): Boolean =
		isHexLiteral(state) && peek(state, 2) == '?'

	private fun hexLiteral(state: State): Long {
		advance(state) // 0
		advance(state) // x
		val result = StringBuilder()
		while (state.currentChar != null && BitOperation.HEX_CHAR.contains(state.currentChar!!.uppercaseChar())) {
			result.append(state.currentChar!!)
			advance(state)
		}
		return BitOperation.hexToLong(result.toString()).toLong()
	}

	private fun undefinedHexLiteral(state: State): DigitalSignal {
		advance(state) // 0
		advance(state) // x
		advance(state) // ?
		if (state.currentChar!!.isDigit()) {
			return DigitalSignalFactory.undefined(BitWidth.of(long(state).toInt()))
		} else {
			throw SyntaxError(state.location, "Expected bit width as number")
		}
	}
}