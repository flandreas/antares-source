package ch.scorpion.antares.dsl

import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslLexer
import ch.scorpion.jabbah.base.dsl.DslTokenType
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.parser.Token

class AntaresLexer(text: String) : DslLexer(text) {

	companion object {
		private val LENGTH_CAST = Token<Unit>(DslTokenType.DOLLAR)
	}

	override fun nextTokenImpl(state: State): Token<Any> {
		when (state.currentChar!!) {
			'$' -> return advanceWith(state, LENGTH_CAST)
		}
		return super.nextTokenImpl(state)
	}

	override fun isLiteral(state: State): Boolean =
		super.isLiteral(state) || isUndefinedHexLiteral(state)

	override fun isNumber(state: State): Boolean =
		isHexLiteral(state) || isBinaryLiteral(state) || super.isNumber(state)

	override fun literal(state: State): Token<Any> =
		when {
			isUndefinedHexLiteral(state) -> literalToken(undefinedHexLiteral(state))
			isHexLiteral(state) -> literalToken(hexLiteral(state))
			isBinaryLiteral(state) -> literalToken(binaryLiteral(state))
			else -> super.literal(state)
		}

	override fun number(state: State): Token<Any> =
		when {
			isHexLiteral(state) -> literalToken(hexLiteral(state))
			isBinaryLiteral(state) -> literalToken(binaryLiteral(state))
			else -> super.number(state)
		}

	private fun isHexLiteral(state: State): Boolean =
		state.currentChar == '0' && peek(state)?.uppercaseChar() == 'X'

	private fun isBinaryLiteral(state: State): Boolean =
		state.currentChar == '0' && peek(state)?.uppercaseChar() == 'B'

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
			throw SyntaxError(state.location, Translations.getString("antares.dsl.expectedBitWidthNumber.msg"))
		}
	}

	private fun binaryLiteral(state: State): DigitalSignal {
		advance(state) // 0
		advance(state) // b
		val result = StringBuilder()
		var hasUndefined = false
		while (state.currentChar != null && BitOperation.BINARY_DIGITS.contains(state.currentChar!!.uppercaseChar())) {
			hasUndefined = hasUndefined || state.currentChar!!.uppercaseChar() == 'Z'
			result.append(state.currentChar!!)
			advance(state)
		}

		return if (hasUndefined) {
			DigitalLiteral.parseBinary(result.toString())
		} else {
			DigitalSignalFactory.of(BitWidth.of(result.toString().length), BitOperation.binaryToLong(result.toString()))
		}
	}
}