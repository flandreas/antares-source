package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test

class BooleanExpressionParserTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldParse() {
		val parser = BooleanExpressionParser(expectAssignment = true, "X = A * B' + A' * B")

		val ast = parser.parse()

		assertAST(ast, """
			Compound
			- =
			-- X
			-- or
			--- and
			---- A
			---- not
			----- B
			--- and
			---- not
			----- A
			---- B
		""".trimIndent())
	}

	@Test
	fun shouldParseWithParenthesis() {
		val parser = BooleanExpressionParser(expectAssignment = true, "X = (A * B') + (A' * B)")

		val ast = parser.parse()

		assertAST(ast, """
			Compound
			- =
			-- X
			-- or
			--- and
			---- A
			---- not
			----- B
			--- and
			---- not
			----- A
			---- B
		""".trimIndent())
	}
}