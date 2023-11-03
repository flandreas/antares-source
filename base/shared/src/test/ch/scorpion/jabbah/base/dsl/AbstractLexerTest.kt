package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.parser.AbstractLexer
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.parser.TokenType
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

abstract class AbstractLexerTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	protected fun assertEof(token: Token<Any>) {
		assertEquals(BaseTokenType.EOF, token.type)
	}

	protected fun assertLong(value: Long, lexer: AbstractLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertString(value: String, lexer: AbstractLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertId(name: String, lexer: AbstractLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.ID, token.type)
		assertEquals(name, token.value)
	}

	protected fun assertToken(type: TokenType, lexer: AbstractLexer) {
		assertEquals(type, lexer.nextToken().type)
	}

	protected fun assertRowColumn(row: Int, column: Int, location: TextLocation) {
		assertEquals(row, location.row, "Wrong row")
		assertEquals(column, location.column, "Wrong column")
	}
}