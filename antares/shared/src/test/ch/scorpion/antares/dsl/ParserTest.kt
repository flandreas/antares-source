package ch.scorpion.antares.dsl

import kotlin.test.Test

class ParserTest {

	@Test
	fun shouldParseExpressionWithParentheses() {
		val parser = Parser("(4 + 12) / -3")
		parser.parse()
	}
}