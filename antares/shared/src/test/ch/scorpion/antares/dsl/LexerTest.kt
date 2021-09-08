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

	private fun assertEof(token: Token<Any>) {
		assertEquals(EOF, token.type)
	}

	private fun assertInt(value: Int, token: Token<Any>) {
		assertEquals(INTEGER, token.type)
		assertEquals(value, token.value)
	}
}