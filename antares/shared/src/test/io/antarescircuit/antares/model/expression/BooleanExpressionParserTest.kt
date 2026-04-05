package io.antarescircuit.antares.model.expression

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.Variable
import io.antarescircuit.jabbah.base.dsl.filterNodes
import kotlin.test.Test
import kotlin.test.assertTrue

class BooleanExpressionParserTest {

	init {
		AntaresTestRule.configure()
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

	@Test
	fun shouldParseSingleCharacterIdentifiers() {
		assertXorAST(BooleanExpressionParser(
			expectAssignment = true,
			"X = AB' + A'B",
			singleCharIdentifier = true
		).parse())
	}

	@Test
	fun shouldParseNegatedArithmeticOutput() {
		val ast = BooleanExpressionParser(expectAssignment = true, "OUT' = A * B' + A' * B").parse()
		assertXorAST(ast, "OUT")
		assertNegatedOutput(ast, "OUT")
	}

	@Test
	fun shouldParseNegatedSingleCharIdentifier() {
		val ast = BooleanExpressionParser(
			expectAssignment = true,
			"X' = A * B' + A' * B",
			singleCharIdentifier = true
		).parse()

		assertXorAST(ast)
		assertNegatedOutput(ast)
	}

	@Test
	fun shouldParseMultiLineSingleCharIdentifier() {
		val ast = BooleanExpressionParser(
			expectAssignment = true,
			"""
				X = A + B
				Y = A 
			""".trimIndent(),
			singleCharIdentifier = true
		).parse()

		assertAST(ast, """
			Compound
			- =
			-- X
			-- or
			--- A
			--- B
			- =
			-- Y
			-- A
		""".trimIndent())
	}

	@Test
	fun shouldParseMultiLineSingleCharIdentifierWithTrailingNegation() {
		val ast = BooleanExpressionParser(
			expectAssignment = true,
			"""
				X = A + B'
				Y' = A 
			""".trimIndent(),
			singleCharIdentifier = true
		).parse()

		assertAST(ast, """
			Compound
			- =
			-- X
			-- or
			--- A
			--- not
			---- B
			- =
			-- Y
			-- A
		""".trimIndent())

		assertNegatedOutput(ast, "Y")
	}

	@Test
	fun shouldParseNegatedArithmeticOutputWithParentheses() {
		val ast = BooleanExpressionParser(expectAssignment = true, "(OUT)' = A * B' + A' * B").parse()
		assertXorAST(ast, "OUT")
		assertNegatedOutput(ast, "OUT")
	}

	@Test
	fun shouldParseNegatedLogicOutput() {
		val ast = BooleanExpressionParser(expectAssignment = true, "¬X = A ∧ ¬B ∨ ¬A ∧ B").parse()
		assertXorAST(ast)
		assertNegatedOutput(ast)
	}

	@Test
	fun shouldParseOutputWithDigit() {
		val ast = BooleanExpressionParser(expectAssignment = true, "O2 = A").parse()
		assertAST(ast, """
			Compound
			- =
			-- O2
			-- A
		""".trimIndent())
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

	private fun assertXorAST(ast: Node, outputName: String = "X") {
		assertAST(ast, """
			Compound
			- =
			-- $outputName
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

	private fun assertNegatedOutput(ast: Node, outputName: String = "X") {
		assertTrue(
			filterNodes(ast) { it is Variable && it.token.value == outputName}
				.map { it as Variable }
				.first().negated)
	}
}