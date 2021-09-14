package ch.scorpion.jabbah.base.dsl

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
		val parser = Parser("""
			var a
			var b
			b = a * (7 - b)
		""".trimIndent())
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
			- Block
			-- =
			--- b
			--- 5
		""".trimIndent())
	}

	@Test
	fun shouldParseVarDeclarationWithoutExpression() {
		val parser = Parser("""
			var a
			a = 5
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- var
			-- a
			- =
			-- a
			-- 5
		""".trimIndent())
	}

	@Test
	fun shouldParseVarDeclarationWithExpression() {
		val parser = Parser("""
			var a = 5
			b = a
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- var
			-- a
			-- 5
			- =
			-- b
			-- a
		""".trimIndent())
	}

	@Test
	fun shouldParseStoreDeclarationWithoutExpression() {
		val parser = Parser("""
			store a
			a = 5
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- store
			-- a
			- =
			-- a
			-- 5
		""".trimIndent())
	}

	@Test
	fun shouldParseStoreDeclarationWithExpression() {
		val parser = Parser("""
			store a = 5
			b = a
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- store
			-- a
			-- 5
			- =
			-- b
			-- a
		""".trimIndent())
	}

	@Test
	fun shouldParseIfThenStatement() {
		val parser = Parser("""
			if (3 == 5) {
				42
			}
		""".trimIndent())

		parser.parse()
	}

	@Test
	fun shouldParseIfThenElseStatement() {
		val parser = Parser("""
			if (3 == 5) {
				42
			} else {
				17
			}
		""".trimIndent())

		parser.parse()
	}

	@Test
	fun shouldParseExpressionWithLogicOperator() {
		val parser = Parser("5 == 5 and 13 == 27")

		assertAST(parser.parse(), """
			Compound
			- and
			-- ==
			--- 5
			--- 5
			-- ==
			--- 13
			--- 27
		""".trimIndent())
	}

	@Test
	fun shouldParseUnaryNot() {
		val parser = Parser("not 3")

		assertAST(parser.parse(), """
			Compound
			- not
			-- 3
		""".trimIndent())
	}

	@Test
	fun shouldParseWhenStatement() {
		val parser = Parser("""
			a = 2
			var b = 0
			when (a) {
				1 : b = 11
				2 : b = 22
				else : b = 99
			}
			b
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- =
			-- a
			-- 2
			- var
			-- b
			-- 0
			- when
			-- a
			-- :
			--- 1
			--- =
			---- b
			---- 11
			-- :
			--- 2
			--- =
			---- b
			---- 22
			-- else
			--- =
			---- b
			---- 99
			- b
		""".trimIndent())
	}

	@Test
	fun shouldParseForStatement() {
		val parser = Parser("""
			for (a in 1 to 10) {
				1
			}
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- for
			-- a
			-- 1
			-- 10
			-- Block
			--- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseAssocArray() {
		val parser = Parser(Lexer("""
			a[27] = 15
			a[28] = 11
		""".trimIndent()), null)

		assertAST(parser.parse(), """
			Compound
			- =
			-- a[]
			--- 27
			-- 15
			- =
			-- a[]
			--- 28
			-- 11
		""".trimIndent())
	}

	@Test
	fun shouldParseAssocArrayAssignments() {
		val parser = Parser(Lexer("a[0] = 1"), null)

		assertAST(parser.parse(), """
			Compound
			- =
			-- a[]
			--- 0
			-- 1
		""".trimIndent())
	}

	private fun assertAST(node: Node, ast: String) {
		val printer = SyntaxTreePrinter()
		node.accept(printer)

		assertEquals(ast, printer.result)
	}
}