package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalLiteral
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.testcase.parser.TestcaseTokenType.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.parser.TokenType

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
	SMALLER("<")
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

		val RESERVED_KEYWORDS = mapOf(
			"run" to RUN_TOKEN
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
			'X','x' -> return if (!statementMode) super.nextTokenImpl(state) else advanceWith(state, DONT_CARE_TOKEN)
			'Z', 'z' -> return if (!statementMode) super.nextTokenImpl(state) else advanceWith(state, UNDEFINED_TOKEN)
			'{' -> return advanceWith(state, LCURLEY_TOKEN)
			'}' -> return advanceWith(state, RCURLEY_TOKEN)
			'>' -> return advanceWith(state, GREATER_TOKEN)
			'<' -> return advanceWith(state, SMALLER_TOKEN)
		}

		return super.nextTokenImpl(state)
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