package io.antarescircuit.antares.model.expression

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.BaseLexer
import io.antarescircuit.jabbah.base.dsl.BaseTokenType
import io.antarescircuit.jabbah.base.parser.Token
import io.antarescircuit.jabbah.base.parser.TokenType
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/**
 * TODO: Copy/Paste from corresponding class in io.antarescircuit.jabbah.base.dsl test package
 * due to missing Kotlin MPP feature KT-35073.
 */
abstract class AbstractLexerTest {

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		Translations.withAnyKey()
	}

	protected fun assertEof(token: Token<Any>) {
		assertEquals(BaseTokenType.EOF, token.type)
	}

	protected fun assertLong(value: Long, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertString(value: String, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertId(name: String, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.ID, token.type)
		assertEquals(name, token.value)
	}

	protected fun assertToken(type: TokenType, lexer: BaseLexer) {
		assertEquals(type, lexer.nextToken().type)
	}
}