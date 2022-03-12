package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.dsl.Node
import kotlin.test.Test

class BooleanExpressionParserTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldParseArithmetic() {
		assertXorAST(BooleanExpressionParser(expectAssignment = true, "X = A * B' + A' * B").parse())
	}

	@Test
	fun shouldParseLogic() {
		assertXorAST(BooleanExpressionParser(expectAssignment = true, "X = A ∧ ¬B ∨ ¬A ∧ B").parse())
	}

	@Test
	fun shouldParseVerbose() {
		assertXorAST(BooleanExpressionParser(expectAssignment = true, "X = A AND NOT B OR NOT A AND B").parse())
		assertXorAST(BooleanExpressionParser(expectAssignment = true, "X = A and not B or not A and B").parse())
	}

	@Test
	fun shouldParseProgramming() {
		assertXorAST(BooleanExpressionParser(expectAssignment = true, "X = A && !B || !A && B").parse())
	}

	@Test
	fun shouldParseArithmeticAssignmentList() {
		assertConstantsAST(BooleanExpressionParser(expectAssignment = true, """
			X = 0
			Y = 1
		""".trimIndent()).parse())
	}

	@Test
	fun shouldParseVerboseAssignmentList() {
		assertConstantsAST(BooleanExpressionParser(expectAssignment = true, """
			X = false
			Y = true
		""".trimIndent()).parse())
	}

	@Test
	fun shouldParseWithParenthesis() {
		assertXorAST(BooleanExpressionParser(expectAssignment = true, "X = (A * B') + (A' * B)").parse())
	}

	private fun assertConstantsAST(ast: Node) {
		assertAST(ast, """
			Compound
			- =
			-- X
			-- false
			- =
			-- Y
			-- true
		""".trimIndent())
	}

	private fun assertXorAST(ast: Node) {
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