package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {

	@Test
	fun shouldScanMultiplyTerm() {
		val lexer = Lexer("7*12")

		assertInt(7, lexer.nextToken())
		assertEquals(MULTIPLY, lexer.nextToken().type)
		assertInt(12, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanDivideTerm() {
		val lexer = Lexer("48/6")

		assertInt(48, lexer.nextToken())
		assertEquals(DIVIDE, lexer.nextToken().type)
		assertInt(6, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanPlusExpression() {
		val lexer = Lexer("12+8")

		assertInt(12, lexer.nextToken())
		assertEquals(PLUS, lexer.nextToken().type)
		assertInt(8, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanMinusExpression() {
		val lexer = Lexer("23-11")

		assertInt(23, lexer.nextToken())
		assertEquals(MINUS, lexer.nextToken().type)
		assertInt(11, lexer.nextToken())
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanTermWithParentheses() {
		val lexer = Lexer("3 * (15 - 7)")

		assertInt(3, lexer.nextToken())
		assertEquals(MULTIPLY, lexer.nextToken().type)
		assertEquals(LPAREN, lexer.nextToken().type)
		assertInt(15, lexer.nextToken())
		assertEquals(MINUS, lexer.nextToken().type)
		assertInt(7, lexer.nextToken())
		assertEquals(RPAREN, lexer.nextToken().type)
		assertEof(lexer.nextToken())
	}

	@Test
	fun shouldScanAssignment() {
		val lexer = Lexer("a = 17")

		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertInt(17, lexer.nextToken())
	}

	@Test
	fun shouldPeekNextToken() {
		val lexer = Lexer("a = 17")

		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.peekNextToken().type)
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertEquals(INTEGER, lexer.peekNextToken().type)
		assertInt(17, lexer.nextToken())
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
		assertInt(5, lexer.nextToken())
		assertEquals(EOL, lexer.nextToken().type)
		assertId("a", lexer.nextToken())
	}

	@Test
	fun shouldSkipMidLineComment() {
		val lexer = Lexer("a = 5 // + 7")
		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertInt(5, lexer.nextToken())
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
		assertEquals(EOL, lexer.nextToken().type)
		assertId("a", lexer.nextToken())
		assertEquals(ASSIGN, lexer.nextToken().type)
		assertInt(12, lexer.nextToken())
		assertEquals(EOL, lexer.nextToken().type)
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

	private fun assertEof(token: Token<Any>) {
		assertEquals(EOF, token.type)
	}

	private fun assertInt(value: Int, token: Token<Any>) {
		assertEquals(INTEGER, token.type)
		assertEquals(value, token.value)
	}

	private fun assertId(value: String, token: Token<Any>) {
		assertEquals(ID, token.type)
		assertEquals(value, token.value)
	}
}