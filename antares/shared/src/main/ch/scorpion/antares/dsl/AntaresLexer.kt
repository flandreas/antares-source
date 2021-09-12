package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.jabbah.base.dsl.Lexer
import ch.scorpion.jabbah.base.dsl.Token

class AntaresLexer(text: String) : Lexer(text) {

	override fun isNumber(state: State): Boolean =
		isHexLiteral(state) || super.isNumber(state)

	override fun number(state: State): Token<Long> =
		when {
			isHexLiteral(state) -> longToken(hexLiteral(state))
			else -> super.number(state)
		}

	private fun isHexLiteral(state: State): Boolean = state.currentChar == '0' && peek(state) == 'x'

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
}