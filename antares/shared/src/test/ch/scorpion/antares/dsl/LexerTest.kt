package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {

	@Test
	fun shouldScanMultiplyTerm() {
		val lexer = Lexer("7*12")

		assertLong(7, lexer.nextToken())
		assertEquals(MULTIPLY, lexer.nextToken().type)
		assertLong(12, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanDivideTerm() {
		val lexer = Lexer("48/6")

		assertLong(48, lexer.nextToken())
		assertEquals(DIVIDE, lexer.nextToken().type)
		assertLong(6, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanPlusExpression() {
		val lexer = Lexer("12+8")

		assertLong(12, lexer.nextToken())
		assertEquals(PLUS, lexer.nextToken().type)
		assertLong(8, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanMinusExpression() {
		val lexer = Lexer("23-11")

		assertLong(23, lexer.nextToken())
		assertEquals(MINUS, lexer.nextToken().type)
		assertLong(11, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanTermWithParentheses() {
		val lexer = Lexer("3 * (15 - 7)")

		assertLong(3, lexer.nextToken())
		assertEquals(MULTIPLY, lexer.nextToken().type)
		assertEquals(LPAREN, lexer.nextToken().type)
		assertLong(15, lexer.nextToken())
		assertEquals(MINUS, lexer.nextToken().type)
		assertLong(7, lexer.nextToken())
		assertEquals(RPAREN, lexer.nextToken().type)
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanAssignment() {
		val lexer = Lexer("a = 17")

		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertLong(17, lexer.nextToken())
	}

	@Test
	fun shouldPeekNextToken() {
		val lexer = Lexer("a = 17")

		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.peekNextToken().type)
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertEquals(LONG, lexer.peekNextToken().type)
		assertLong(17, lexer.nextToken())
	}

	@Test
	fun shouldSkipStartLineComment() {
		val lexer = Lexer("""
			a = 5
			// a = 12
			a
		""".trimIndent())

		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertLong(5, lexer.nextToken())
		assertId("a", lexer.nextToken())
	}

	@Test
	fun shouldSkipMidLineComment() {
		val lexer = Lexer("a = 5 // + 7")
		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertLong(5, lexer.nextToken())
		assertEquals(EOF, lexer.nextToken().type)
	}

	@Test
	fun shouldScanBlock() {
		val lexer = Lexer("""
			{
				a = 12
			}
		""".trimIndent())

		assertEquals(LCURLEY, lexer.nextToken().type)
		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertLong(12, lexer.nextToken())
		assertEquals(RCURLEY, lexer.nextToken().type)
	}

	@Test
	fun shouldScanVar() {
		val lexer = Lexer("var a")

		assertEquals(VAR, lexer.nextToken().type)
		assertId("a", lexer.nextToken())
	}

	@Test
	fun shouldScanEqual() {
		val lexer = Lexer("a == b")
		assertId("a", lexer.nextToken())
		assertEquals(EQUAL, lexer.nextToken().type)
		assertId("b", lexer.nextToken())
	}

	@Test
	fun shouldScanDiff() {
		val lexer = Lexer("a != b")
		assertId("a", lexer.nextToken())
		assertEquals(DIFF, lexer.nextToken().type)
		assertId("b", lexer.nextToken())
	}

	@Test
	fun shouldScanSmaller() {
		val lexer = Lexer("a < b")
		assertId("a", lexer.nextToken())
		assertEquals(SMALLER, lexer.nextToken().type)
		assertId("b", lexer.nextToken())
	}

	@Test
	fun shouldScanGreater() {
		val lexer = Lexer("a > b")
		assertId("a", lexer.nextToken())
		assertEquals(GREATER, lexer.nextToken().type)
		assertId("b", lexer.nextToken())
	}

	@Test
	fun shouldScanSmallerEqual() {
		val lexer = Lexer("a <= b")
		assertId("a", lexer.nextToken())
		assertEquals(SMALLER_EQUAL, lexer.nextToken().type)
		assertId("b", lexer.nextToken())
	}

	@Test
	fun shouldScanGreaterEqual() {
		val lexer = Lexer("a >= b")
		assertId("a", lexer.nextToken())
		assertEquals(GREATER_EQUAL, lexer.nextToken().type)
		assertId("b", lexer.nextToken())
	}

	@Test
	fun shouldScanIfThen() {
		val lexer = Lexer("if (5) {}")
		assertEquals(IF, lexer.nextToken().type)
		assertEquals(LPAREN, lexer.nextToken().type)
		assertLong(5, lexer.nextToken())
		assertEquals(RPAREN, lexer.nextToken().type)
		assertEquals(LCURLEY, lexer.nextToken().type)
		assertEquals(RCURLEY, lexer.nextToken().type)
	}

	@Test
	fun shouldScanIfThenElse() {
		val lexer = Lexer("if (5) { 17 } else { 42 }")
		assertEquals(IF, lexer.nextToken().type)
		assertEquals(LPAREN, lexer.nextToken().type)
		assertLong(5, lexer.nextToken())
		assertEquals(RPAREN, lexer.nextToken().type)
		assertEquals(LCURLEY, lexer.nextToken().type)
		assertLong(17, lexer.nextToken())
		assertEquals(RCURLEY, lexer.nextToken().type)
		assertEquals(ELSE, lexer.nextToken().type)
		assertEquals(LCURLEY, lexer.nextToken().type)
		assertLong(42, lexer.nextToken())
		assertEquals(RCURLEY, lexer.nextToken().type)
	}

	@Test
	fun shouldScanAnd() {
		val lexer = Lexer("5 and 7")
		assertLong(5, lexer.nextToken())
		assertEquals(AND, lexer.nextToken().type)
		assertLong(7, lexer.nextToken())
	}

	@Test
	fun shouldScanOr() {
		val lexer = Lexer("5 or 7")
		assertLong(5, lexer.nextToken())
		assertEquals(OR, lexer.nextToken().type)
		assertLong(7, lexer.nextToken())
	}

	@Test
	fun shouldScanNot() {
		val lexer = Lexer("not 2")
		assertEquals(NOT, lexer.nextToken().type)
		assertLong(2, lexer.nextToken())
	}

	@Test
	fun shouldScanShiftLeft() {
		val lexer = Lexer("4 << 1")
		assertLong(4, lexer.nextToken())
		assertEquals(SHIFT_LEFT, lexer.nextToken().type)
		assertLong(1, lexer.nextToken())
	}

	@Test
	fun shouldScanShiftRight() {
		val lexer = Lexer("4 >> 1")
		assertLong(4, lexer.nextToken())
		assertEquals(SHIFT_RIGHT, lexer.nextToken().type)
		assertLong(1, lexer.nextToken())
	}

	@Test
	fun shouldScanMod() {
		val lexer = Lexer("5 % 2")
		assertLong(5, lexer.nextToken())
		assertEquals(MOD, lexer.nextToken().type)
		assertLong(2, lexer.nextToken())
	}

	@Test
	fun shouldScanHexLiteral() {
		assertHexLiteral(255, "0xFF")
		assertHexLiteral(255, "0xff")
		assertHexLiteral(7006, "0x1B5E")
	}

	private fun assertHexLiteral(expected: Long, literal: String) {
		val lexer = Lexer("a = $literal")
		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		val token = lexer.nextToken()
		assertEquals(LONG, token.type)
		assertEquals(expected, token.value)
	}

	private fun assertEof(token: Token<Any>) {
		assertEquals(EOF, token.type)
	}

	private fun assertLong(value: Long, token: Token<Any>) {
		assertEquals(LONG, token.type)
		assertEquals(value, token.value)
	}

	private fun assertId(value: String, token: Token<Any>) {
		assertEquals(ID, token.type)
		assertEquals(value, token.value)
	}
}