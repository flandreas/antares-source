package ch.scorpion.antares.model.expression

import kotlin.test.Ignore
import kotlin.test.Test

class BooleanExpressionParserTest {

	@Ignore
	@Test
	fun shouldParse() {
		val parser = BooleanExpressionParser("X = A * B' + A' * B")

		val ast = parser.parse()

		assertAST(ast, """
			=
			- X
			- or
			-- and
			--- A
			--- not
			---- B
			-- and
			--- not
			---- A
			--- B
		""".trimIndent())
	}
}