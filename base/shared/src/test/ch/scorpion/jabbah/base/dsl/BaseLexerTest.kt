package ch.scorpion.jabbah.base.dsl

import kotlin.test.Test

class BaseLexerTest : AbstractLexerTest() {

	@Test
	fun shouldParseIdentifiers() {
		val lexer = BaseLexer("These are identifiers")

		assertId("These", lexer)
		assertId("are", lexer)
		assertId("identifiers", lexer)
	}

	@Test
	fun shouldSkipStartLineComment() {
		val lexer = BaseLexer("""
			// Comment
			Hello
		""".trimIndent())

		assertId("Hello", lexer)
		assertToken(BaseTokenType.EOF, lexer)
	}

	@Test
	fun shouldSkipMidLineComment() {
		val lexer = BaseLexer("""
			Hello// Comment
		""".trimIndent())

		assertId("Hello", lexer)
		assertToken(BaseTokenType.EOF, lexer)
	}
}