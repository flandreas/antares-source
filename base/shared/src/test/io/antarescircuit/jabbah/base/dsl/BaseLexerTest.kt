package io.antarescircuit.jabbah.base.dsl

import kotlin.test.Test

class BaseLexerTest : AbstractLexerTest() {

	@Test
	fun shouldParseIdentifiers() {
		val lexer = BaseLexer("These are identifiers99")

		assertId("These", lexer)
		assertId("are", lexer)
		assertId("identifiers99", lexer)
	}

	@Test
	fun shouldSupportSpecialCharactersInIdentifiers() {
		val lexer = BaseLexer("A_B_")

		assertId("A_B_", lexer)
	}

	@Test
	fun shouldReportLocation() {
		val lexer = BaseLexer("""
			11
			21 22
			31
		""".trimIndent())

		assertLong(11, lexer)
		assertRowColumn(1, 1, lexer.location)

		assertLong(21, lexer)
		assertRowColumn(2, 1, lexer.location)

		assertLong(22, lexer)
		assertRowColumn(2, 4, lexer.location)

		assertLong(31, lexer)
		assertRowColumn(3, 1, lexer.location)
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

	@Test
	fun shouldIncrementRowOnComment() {
		val lexer = BaseLexer("""
			Line1
			// Comment
			Line3
			Line4
		""".trimIndent())

		assertId("Line1", lexer)
		assertRowColumn(1, 1, lexer.location)

		assertId("Line3", lexer)
		assertRowColumn(3, 1, lexer.location)

		assertId("Line4", lexer)
		assertRowColumn(4, 1, lexer.location)
	}

	@Test
	fun shouldParseLong() {
		val lexer = BaseLexer("1244")
		assertLong(1244L, lexer)
	}

	@Test
	fun shouldParseFloat() {
		val lexer = BaseLexer("3.14")
		assertFloat(3.14F, lexer)
	}
}