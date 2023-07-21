package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.BaseLexer
import ch.scorpion.jabbah.base.parser.Token
import ch.scorpion.jabbah.base.dsl.DslTokenType
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/**
 * TODO: Copy/Paste from corresponding class in ch.scorpion.jabbah.base.dsl test package
 * due to missing Kotlin MPP feature KT-35073.
 */
abstract class AbstractLexerTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	protected fun assertEof(token: Token<Any>) {
		assertEquals(DslTokenType.EOF, token.type)
	}

	protected fun assertLong(value: Long, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(DslTokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertString(value: String, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(DslTokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}

	protected fun assertId(name: String, lexer: BaseLexer) {
		val token = lexer.nextToken()
		assertEquals(DslTokenType.ID, token.type)
		assertEquals(name, token.value)
	}

	protected fun assertToken(type: DslTokenType, lexer: BaseLexer) {
		assertEquals(type, lexer.nextToken().type)
	}
}