package ch.scorpion.antares.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserTest {

	@Test
	fun shouldParseExpressionWithParentheses() {
		val parser = Parser("(4 + 12) / -3")
		parser.parse()
	}

	@Test
	fun shouldParseAssignment() {
		val parser = Parser("a = 5")
		val ast = parser.parse()

		val assignment = (ast as Compound).children.first() as Assignment
		assertEquals("a", assignment.left.token.value)
		assertEquals(TokenType.ASSIGN, assignment.op.type)
		assertIs<Number>(assignment.right)
	}

	@Test
	fun shouldParseExpressionWithVariables() {
		val parser = Parser("a * (7 - b)")
		parser.parse()
	}

	@Test
	fun shouldParseEmptyLinesBetweenStatements() {
		val parser = Parser("""
			a = 5
			
			b = 12
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- =
			-- a
			-- 5
			- =
			-- b
			-- 12
		""".trimIndent())
	}

	@Test
	fun shouldParseBlock() {
		val parser = Parser("""
			{
				b = 5
			}
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- Compound
			-- =
			--- b
			--- 5
		""".trimIndent())
	}

	@Test
	fun shouldParseDeclarationWithoutExpression() {
		val parser = Parser("""
			var a
			a = 5
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- Var
			-- a
			- =
			-- a
			-- 5
		""".trimIndent())
	}

	@Test
	fun shouldParseDeclarationWithExpression() {
		val parser = Parser("""
			var a = 5
			b = a
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- Var
			-- a
			-- 5
			- =
			-- b
			-- a
		""".trimIndent())
	}

	private fun assertAST(node: Node, ast: String) {
		val printer = SyntaxTreePrinter()
		node.accept(printer)

		assertEquals(ast, printer.result)
	}
}