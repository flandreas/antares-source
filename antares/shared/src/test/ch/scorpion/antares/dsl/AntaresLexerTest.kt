package ch.scorpion.antares.dsl

import ch.scorpion.jabbah.base.dsl.Lexer
import ch.scorpion.jabbah.base.dsl.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class AntaresLexerTest {

	@Test
	fun shouldScanHexLiteral() {
		assertHexLiteral(255, "0xFF")
		assertHexLiteral(255, "0xff")
		assertHexLiteral(7006, "0x1B5E")
	}

	private fun assertHexLiteral(expected: Long, literal: String) {
		val lexer = AntaresLexer("a = $literal")
		assertId("a", lexer)
		assertToken(TokenType.ASSIGN, lexer)
		val token = lexer.nextToken()
		assertEquals(TokenType.LONG, token.type)
		assertEquals(expected, token.value)
	}

	private fun assertId(name: String, lexer: Lexer) {
		val token = lexer.nextToken()
		assertEquals(TokenType.ID, token.type)
		assertEquals(name, token.value)
	}

	private fun assertToken(type: TokenType, lexer: Lexer) {
		assertEquals(type, lexer.nextToken().type)
	}
}