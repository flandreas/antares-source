package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.dsl.TokenType.*

/**
 * Extends [BaseLexer] with tokens for simple languages supporting arithmetic expressions,
 * control flow statements, and variables.
 *
 * @property text the text to be scanned
 */
open class Lexer(text: String): BaseLexer(text) {

	companion object {

		// Singleton instances of value-less [Token]s
		private val PLUS_TOKEN = Token<Unit>(PLUS)
		private val MINUS_TOKEN = Token<Unit>(MINUS)
		private val MULTIPLY_TOKEN = Token<Unit>(MULTIPLY)
		private val DIVIDE_TOKEN = Token<Unit>(DIVIDE)
		private val LPAREN_TOKEN = Token<Unit>(LPAREN)
		private val RPAREN_TOKEN = Token<Unit>(RPAREN)
		private val ASSIGN_TOKEN = Token<Unit>(ASSIGN)
		private val LCURLEY_TOKEN = Token<Unit>(LCURLEY)
		private val RCURLEY_TOKEN = Token<Unit>(RCURLEY)
		private val VAR_TOKEN = Token<String>(VAR)
		private val STORE_TOKEN = Token<String>(STORE)
		private val EQUAL_TOKEN = Token<Unit>(EQUAL)
		private val DIFF_TOKEN = Token<Unit>(DIFF)
		private val IF_TOKEN = Token<String>(IF)
		private val ELSE_TOKEN = Token<String>(ELSE)
		val AND_TOKEN = Token<String>(AND)
		val OR_TOKEN = Token<String>(OR)
		val NOT_TOKEN = Token<String>(NOT)
		private val GREATER_TOKEN = Token<Unit>(GREATER)
		private val GREATER_EQUAL_TOKEN = Token<Unit>(GREATER_EQUAL)
		private val SMALLER_TOKEN = Token<Unit>(SMALLER)
		private val SMALLER_EQUAL_TOKEN = Token<Unit>(SMALLER_EQUAL)
		private val SHIFT_LEFT_TOKEN = Token<Unit>(SHIFT_LEFT)
		private val SHIFT_RIGHT_TOKEN = Token<Unit>(SHIFT_RIGHT)
		private val MOD_TOKEN = Token<String>(MOD)
		private val WHEN_TOKEN = Token<String>(WHEN)
		private val COLON_TOKEN = Token<String>(COLON)
		private val FOR_TOKEN = Token<String>(FOR)
		private val IN_TOKEN = Token<String>(IN)
		private val TO_TOKEN = Token<String>(TO)
		private val CARET_TOKEN = Token<String>(CARET)
		private val LEFT_BRACKET_TOKEN = Token<Unit>(LEFT_BRACKET)
		private val RIGHT_BRACKET_TOKEN = Token<Unit>(RIGHT_BRACKET)
		private val QUESTION_MARK_TOKEN = Token<Unit>(QUESTION_MARK)
		private val RETURN_TOKEN = Token<String>(RETURN)
		private val INIT_TOKEN = Token<String>(INIT)
		private val AT_TOKEN = Token<Unit>(AT)
		private val HASH_TOKEN = Token<Unit>(HASH)
		private val DOT_TOKEN = Token<Unit>(DOT)
		private val COMMA_TOKEN = Token<Unit>(COMMA)

		val RESERVED_KEYWORDS = mapOf(
			"var" to VAR_TOKEN,
			"store" to STORE_TOKEN,
			"if" to IF_TOKEN,
			"else" to ELSE_TOKEN,
			"and" to AND_TOKEN,
			"or" to OR_TOKEN,
			"not" to NOT_TOKEN,
			"when" to WHEN_TOKEN,
			"for" to FOR_TOKEN,
			"in" to IN_TOKEN,
			"to" to TO_TOKEN,
			"return" to RETURN_TOKEN,
			"init" to INIT_TOKEN
		)

		fun getReservedWords(): Collection<String> = RESERVED_KEYWORDS.keys
	}

	override fun getReservedKeyword(name: String): Token<String>? =
		RESERVED_KEYWORDS[name] ?: super.getReservedKeyword(name)

	override fun nextTokenImpl(state: State): Token<Any> {
		if (isEqual(state)) {
			return equal(state)
		}

		if (isDifferent(state)) {
			return diff(state)
		}

		if (isSmallerEqual(state)) {
			return smallerEqual(state)
		}

		if (isGreaterEqual(state)) {
			return greaterEqual(state)
		}

		if (isShiftLeft(state)) {
			return shiftLeft(state)
		}

		if (isShiftRight(state)) {
			return shiftRight(state)
		}

		when (state.currentChar!!) {
			'/' -> return advanceWith(state, DIVIDE_TOKEN)
			'+' -> return advanceWith(state, PLUS_TOKEN)
			'-' -> return advanceWith(state, MINUS_TOKEN)
			'*' -> return advanceWith(state, MULTIPLY_TOKEN)
			'(' -> return advanceWith(state, LPAREN_TOKEN)
			')' -> return advanceWith(state, RPAREN_TOKEN)
			'=' -> return advanceWith(state, ASSIGN_TOKEN)
			'{' -> return advanceWith(state, LCURLEY_TOKEN)
			'}' -> return advanceWith(state, RCURLEY_TOKEN)
			'<' -> return advanceWith(state, SMALLER_TOKEN)
			'>' -> return advanceWith(state, GREATER_TOKEN)
			'%' -> return advanceWith(state, MOD_TOKEN)
			':' -> return advanceWith(state, COLON_TOKEN)
			'^' -> return advanceWith(state, CARET_TOKEN)
			'[' -> return advanceWith(state, LEFT_BRACKET_TOKEN)
			']' -> return advanceWith(state, RIGHT_BRACKET_TOKEN)
			'?' -> return advanceWith(state, QUESTION_MARK_TOKEN)
			'@' -> return advanceWith(state, AT_TOKEN)
			'#' -> return advanceWith(state, HASH_TOKEN)
			'.' -> return advanceWith(state, DOT_TOKEN)
			',' -> return advanceWith(state, COMMA_TOKEN)
		}

		return super.nextTokenImpl(state)
	}

	private fun isEqual(state: State): Boolean = state.currentChar == '=' && peek(state) == '='

	private fun isDifferent(state: State): Boolean = state.currentChar == '!' && peek(state) == '='

	private fun isSmallerEqual(state: State): Boolean = state.currentChar == '<' && peek(state) == '='

	private fun isGreaterEqual(state: State): Boolean = state.currentChar == '>' && peek(state) == '='

	private fun isShiftLeft(state: State): Boolean = state.currentChar == '<' && peek(state) == '<'

	private fun isShiftRight(state: State): Boolean = state.currentChar == '>' && peek(state) == '>'

	private fun equal(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return EQUAL_TOKEN
	}

	private fun diff(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return DIFF_TOKEN
	}

	private fun smallerEqual(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return SMALLER_EQUAL_TOKEN
	}

	private fun greaterEqual(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return GREATER_EQUAL_TOKEN
	}

	private fun shiftLeft(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return SHIFT_LEFT_TOKEN
	}

	private fun shiftRight(state: State): Token<Unit> {
		advance(state)
		advance(state)
		return SHIFT_RIGHT_TOKEN
	}
}