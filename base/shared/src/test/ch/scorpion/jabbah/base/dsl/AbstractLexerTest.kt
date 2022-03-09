package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

abstract class AbstractLexerTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	protected fun assertEof(token: Token<Any>) {
		assertEquals(TokenType.EOF, token.type)
	}

	protected fun assertLong(value: Long, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(TokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertString(value: String, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(TokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertId(name: String, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(TokenType.ID, token.type)
		assertEquals(name, token.value)
	}

	protected fun assertToken(type: TokenType, lexer: BaseLexer) {
		assertEquals(type, lexer.nextToken().type)
	}
}