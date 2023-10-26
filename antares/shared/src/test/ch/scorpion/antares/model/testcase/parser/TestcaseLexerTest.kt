package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.jabbah.base.dsl.BaseTokenType
import ch.scorpion.jabbah.base.parser.AbstractLexer
import ch.scorpion.jabbah.base.parser.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class TestcaseLexerTest {

	@Test
	fun shouldScanRun() {
		val lexer = TestcaseLexer("""
			run {
				0 1
			}
		""".trimIndent())

		assertToken(TestcaseTokenType.RUN, lexer)
		assertToken(TestcaseTokenType.LCURLEY, lexer)
		assertToken(BaseTokenType.EOL, lexer)
		assertLong(0L, lexer)
		assertLong(1L, lexer)
		assertToken(BaseTokenType.EOL, lexer)
		assertToken(TestcaseTokenType.RCURLEY, lexer)
	}

	private fun assertToken(type: TokenType, lexer: AbstractLexer) {
		assertEquals(type, lexer.nextToken().type)
	}

	private fun assertLong(value: Long, lexer: AbstractLexer) {
		val token = lexer.nextToken()
		assertEquals(BaseTokenType.LITERAL, token.type)
		assertEquals(value, token.value)
	}
}