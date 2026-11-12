package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.model.signal.BitOperation
import io.antarescircuit.antares.model.signal.DigitalLiteral
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.testcase.parser.TestcaseTokenType.*
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.BaseLexer
import io.antarescircuit.jabbah.base.dsl.SyntaxError
import io.antarescircuit.jabbah.base.parser.Token
import io.antarescircuit.jabbah.base.parser.TokenType

enum class TestcaseTokenType(override val id: String) : TokenType {
	DONT_CARE("X"),
	UNDEFINED("Z"),
	CARET("^"),
	RUN("run"),
	LCURLEY("{"),
	RCURLEY("}"),
	DECIMAL_LITERAL("0d"),
	BINARY_LITERAL("0b"),
	HEX_LITERAL("0x"),
	GREATER(">"),
	SMALLER("<"),
	LOOP("loop"),
	RANGE(".."),
	LPAREN("("),
	RPAREN(")"),
	PLUS("+"),
	MINUS("-"),
	MULTIPLY("*"),
	AMPERSAND("&"),
	VERTICAL_BAR("|"),
	TILDE("~"),
	SHIFT_LEFT("<<"),
	SHIFT_RIGHT(">>"),
}

class TestcaseLexer(text: String) : BaseLexer(text) {

	companion object {
		private val CARET_TOKEN = Token<Unit>(CARET)
		private val DONT_CARE_TOKEN = Token<Unit>(DONT_CARE)
		private val UNDEFINED_TOKEN = Token<Unit>(UNDEFINED)
		private val RUN_TOKEN = Token<String>(RUN)
		private val LCURLEY_TOKEN = Token<Unit>(LCURLEY)
		private val RCURLEY_TOKEN = Token<Unit>(RCURLEY)
		private val GREATER_TOKEN = Token<Unit>(GREATER)
		private val SMALLER_TOKEN = Token<Unit>(SMALLER)
		private val LOOP_TOKEN = Token<String>(LOOP)
		private val RANGE_TOKEN = Token<Unit>(RANGE)
		private val LPAREN_TOKEN = Token<Unit>(LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(RPAREN)
		private val PLUS_TOKEN = Token<Unit>(PLUS)
		private val MINUS_TOKEN = Token<Unit>(MINUS)
		private val MULTIPLY_TOKEN = Token<Unit>(MULTIPLY)
		private val AMPERSAND_TOKEN = Token<Unit>(AMPERSAND)
		private val VERTICAL_BAR_TOKEN = Token<Unit>(VERTICAL_BAR)
		private val TILDE_TOKEN = Token<Unit>(TILDE)
		private val SHIFT_LEFT_TOKEN = Token<Unit>(SHIFT_LEFT)
		private val SHIFT_RIGHT_TOKEN = Token<Unit>(SHIFT_RIGHT)

		val RESERVED_KEYWORDS = mapOf(
			"run" to RUN_TOKEN,
			"loop" to LOOP_TOKEN,
		)

		fun decimalLiteralToken(value: DigitalSignal) = Token(DECIMAL_LITERAL, value)
		fun binaryLiteralToken(value: DigitalSignal) = Token(BINARY_LITERAL, value)
		fun hexLiteralToken(value: DigitalSignal) = Token(HEX_LITERAL, value)
	}

	/**
	 * Helps to distinguish whether the lexer scans symbols for the "header" or any of the "statements" rows.
	 * In headers (port names), "X" and "Z" are to be treated as IDs. In statements, they are to be treated
	 * as special symbols.
	 * </p>
	 * Initialize with `false` because the super class calls [nextTokenImpl] from the constructor, where
	 * this property is not yet initialized and therefore `false`. Set to `true` when the newline
	 * completing the header line gets scanned.
	 */
	private var statementMode: Boolean = false

	override fun getReservedKeyword(name: String): Token<String>? =
		RESERVED_KEYWORDS[name] ?: super.getReservedKeyword(name)

	override fun nextTokenImpl(state: State): Token<Any> {
		when (state.currentChar!!) {
			'\n' -> {
				statementMode = true
				return advanceWith(state, EOL_TOKEN)
			}
			'^' -> return advanceWith(state, CARET_TOKEN)
			// A standalone X/Z is a special value, otherwise it starts an identifier (e.g. a loop variable)
			'X','x' -> return if (!statementMode || isIdChar(peek(state))) super.nextTokenImpl(state) else advanceWith(state, DONT_CARE_TOKEN)
			'Z', 'z' -> return if (!statementMode || isIdChar(peek(state))) super.nextTokenImpl(state) else advanceWith(state, UNDEFINED_TOKEN)
			'{' -> return advanceWith(state, LCURLEY_TOKEN)
			'}' -> return advanceWith(state, RCURLEY_TOKEN)
			'>' -> return if (peek(state) == '>') advanceTwiceWith(state, SHIFT_RIGHT_TOKEN) else advanceWith(state, GREATER_TOKEN)
			'<' -> return if (peek(state) == '<') advanceTwiceWith(state, SHIFT_LEFT_TOKEN) else advanceWith(state, SMALLER_TOKEN)
			'.' -> if (peek(state) == '.') return advanceTwiceWith(state, RANGE_TOKEN)
			'(' -> return advanceWith(state, LPAREN_TOKEN)
			')' -> return advanceWith(state, RPAREN_TOKEN)
			'+' -> return advanceWith(state, PLUS_TOKEN)
			'-' -> return advanceWith(state, MINUS_TOKEN)
			'*' -> return advanceWith(state, MULTIPLY_TOKEN)
			'&' -> return advanceWith(state, AMPERSAND_TOKEN)
			'|' -> return advanceWith(state, VERTICAL_BAR_TOKEN)
			'~' -> return advanceWith(state, TILDE_TOKEN)
		}

		return super.nextTokenImpl(state)
	}

	private fun isIdChar(c: Char?): Boolean = c != null && (c.isLetterOrDigit() || c == '_')

	private fun advanceTwiceWith(state: State, token: Token<Any>): Token<Any> {
		advance(state)
		return advanceWith(state, token)
	}

	/** Testcases are line-oriented, so newlines are not whitespace.*/
	override fun isWhitespace(state: State): Boolean =
		super.isWhitespace(state) && state.currentChar != '\n'

	override fun isNumber(state: State): Boolean =
		isHexIdentifier(state) || isBinaryIdentifier(state) || super.isNumber(state)

	override fun number(state: State): Token<Any> =
		when {
			isHexIdentifier(state) -> hexLiteral(state)
			isBinaryIdentifier(state) -> binaryLiteral(state)
			else -> decimalLiteral(state)
		}

	private fun isHexIdentifier(state: State): Boolean = state.currentChar == '0' && peek(state)?.uppercaseChar() == 'X'

	private fun isBinaryIdentifier(state: State): Boolean = state.currentChar == '0' && peek(state)?.uppercaseChar() == 'B'

	private fun decimalLiteral(state: State): Token<DigitalSignal> =
		decimalLiteralToken(DigitalSignalFactory.ofMinimalBitWidth(long(state).toULong()))

	private fun hexLiteral(state: State): Token<DigitalSignal> {
		advance(state)
		advance(state)
		val result = StringBuilder()
		while (state.currentChar != null && BitOperation.HEX_CHAR.contains(state.currentChar!!.uppercaseChar())) {
			result.append(state.currentChar!!)
			advance(state)
		}

		try {
			return hexLiteralToken(DigitalLiteral.parseHex(result.toString()))
		} catch (e: Throwable) {
			throw SyntaxError(location, Translations.getString("antares.testcase.error.invalidHex"))
		}
	}

	private fun binaryLiteral(state: State): Token<DigitalSignal> {
		advance(state) // 0
		advance(state) // b
		val result = StringBuilder()
		while (state.currentChar != null && ( state.currentChar!!.uppercaseChar() == 'Z' || BitOperation.BINARY_DIGITS.contains(state.currentChar!!.uppercaseChar()))) {
			result.append(state.currentChar!!)
			advance(state)
		}

		try {
			return binaryLiteralToken(DigitalLiteral.parseBinary(result.toString()))
		} catch (e: Throwable) {
			throw SyntaxError(location, Translations.getString("antares.testcase.error.invalidBinary"))
		}
	}
}