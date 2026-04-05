package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.Translations
import kotlin.test.*

class DslParserTest {

	@BeforeTest
	fun setup() {
		Translations.withAnyKey()
	}

	@Test
	fun shouldParseExpressionWithParentheses() {
		val parser = DslParser("(4 + 12) / -3")
		parser.parse()
	}

	@Test
	fun shouldParseFloatOperation() {
		val parser = DslParser("0.77 - 9")

		assertAST(parser.parse(), """
			Compound
			- -
			-- 0.77
			-- 9
		""".trimIndent())
	}

	@Test
	fun shouldParseAssignment() {
		val parser = DslParser("""
			var a
			a = 5
		""".trimIndent())
		val ast = parser.parse()

		val assignment = (ast as Compound<*>).children.first { it is Assignment } as Assignment
		assertEquals("a", assignment.left.token.value)
		assertIs<Literal>(assignment.right)
	}

	@Test
	fun shouldRejectKeywordVariableName() {
		assertFailsWith(SyntaxError::class) {
			DslParser(DslLexer("""
				in = 42				
			""".trimIndent())).parse()
		}
	}

	@Test
	fun shouldParseExpressionWithVariables() {
		val parser = DslParser("""
			var a
			var b
			b = a * (7 - b)
		""".trimIndent())
		parser.parse()
	}

	@Test
	fun shouldParseEmptyLinesBetweenStatements() {
		val parser = DslParser("""
			var a = 5
			
			var b = 12
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- var
			-- a
			-- 5
			- var
			-- b
			-- 12
		""".trimIndent())
	}

	@Test
	fun shouldParseBlock() {
		val parser = DslParser("""
			{
				var b = 5
			}
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- Block
			-- var
			--- b
			--- 5
		""".trimIndent())
	}

	@Test
	fun shouldParseVarDeclarationWithoutExpression() {
		val parser = DslParser("""
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
		val parser = DslParser("""
			var a = 5
			var b = a
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- var
			-- a
			-- 5
			- var
			-- b
			-- a
		""".trimIndent())
	}

	@Test
	fun shouldParseStoreDeclarationWithoutExpression() {
		val parser = DslParser("""
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
		val parser = DslParser("""
			store a = 5
			store b = a
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- store
			-- a
			-- 5
			- store
			-- b
			-- a
		""".trimIndent())
	}

	@Test
	fun shouldParseIfThenStatement() {
		val parser = DslParser("""
			if (3 == 5) {
				42
			}
		""".trimIndent())

		parser.parse()
	}

	@Test
	fun shouldParseIfThenElseStatement() {
		val parser = DslParser("""
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
		val parser = DslParser("5 == 5 and 13 == 27")

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
	fun shouldParseComplexLogicExpression() {
		val parser = DslParser("""
			var A = 0
			var B = 1
			var O = A and not B or not A and B
		""".trimIndent())
		assertAST(parser.parse(), """
			Compound
			- var
			-- A
			-- 0
			- var
			-- B
			-- 1
			- var
			-- O
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
	fun shouldParseUnaryNot() {
		val parser = DslParser("not 3")

		assertAST(parser.parse(), """
			Compound
			- not
			-- 3
		""".trimIndent())
	}

	@Test
	fun shouldParseWhenStatement() {
		val parser = DslParser("""
			var a = 2
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
			- var
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
	fun shouldReportLocation() {
		val ast = DslParser("""
			var a = 2
			var b = 0
			// This is a WHEN statement
			when (a) {
				1 : b = 11
				2 : b = 22
				else : b = 99
			}
			b
		""".trimIndent()).parse() as Compound<*>

		assertRowColumn( 1, 1, ast.location)
		assertRowColumn(1, 1, ast.children[0].location)
		assertRowColumn(2, 1, ast.children[1].location)
		assertRowColumn(4, 1, ast.children[2].location)
		assertRowColumn(5, 2, (ast.children[2] as WhenStatement).clauses[0].location)
	}

	@Test
	fun shouldParseForStatement() {
		val parser = DslParser("""
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
		val parser = DslParser(DslLexer("""
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
		val parser = DslParser(DslLexer("a[0] = 1"), null)

		assertAST(parser.parse(), """
			Compound
			- =
			-- a[]
			--- 0
			-- 1
		""".trimIndent())
	}

	@Test
	fun shouldParseAssocArrayWithExpressionIndex() {
		val parser = DslParser(DslLexer("a[1+2]"), null)

		assertAST(parser.parse(), """
			Compound
			- a[]
			-- +
			--- 1
			--- 2
		""".trimIndent())
	}

	@Test
	fun shouldParseAssocArrayWithVariableIndex() {
		val parser = DslParser(DslLexer("""
			a[b]
		""".trimIndent()), null)

		assertAST(parser.parse(), """
			Compound
			- a[]
			-- b
		""".trimIndent())
	}

	@Test
	fun shouldParseFunctionCallWithParameters() {
		val parser = DslParser(DslLexer("f(1, 2)"), null)

		assertAST(parser.parse(), """
			Compound
			- f()
			-- 1
			-- 2
		""".trimIndent())
	}

	@Test
	fun shouldParseFunctionCallWithoutParameters() {
		val parser = DslParser(DslLexer("f()"), null)

		assertAST(parser.parse(), """
			Compound
			- f()
		""".trimIndent())
	}

	@Test
	fun shouldParseStringLiteral() {
		val parser = DslParser(DslLexer("a = \"text\""), null)

		assertAST(parser.parse(), """
			Compound
			- =
			-- a
			-- text
		""".trimIndent())
	}

	@Test
	fun shouldParseFunctionCallWithStringParam() {
		val parser = DslParser(DslLexer("f(\"A+B\")"), null)

		assertAST(parser.parse(), """
			Compound
			- f()
			-- A+B
		""".trimIndent())
	}

	@Test
	fun shouldParseExponentialTerm() {
		val parser = DslParser(DslLexer("2^3"), null)

		assertAST(parser.parse(), """
			Compound
			- ^
			-- 2
			-- 3
		""".trimIndent())
	}

	@Test
	fun shouldParseExponentialTermWithVariable() {
		val parser = DslParser("""
			var a = 4
			2^a
		""".trimIndent())

		assertAST(parser.parse(), """
			Compound
			- var
			-- a
			-- 4
			- ^
			-- 2
			-- a
		""".trimIndent())
	}

	@Test
	fun shouldParseShiftWithVariableRightFactor() {
		val parser = DslParser(DslLexer("A << B"), null)

		assertAST(parser.parse(), """
			Compound
			- <<
			-- A
			-- B
		""".trimIndent())
	}

	@Test
	fun comparisonShouldHavePrecedenceOverTerms() {
		val parser = DslParser(DslLexer("A + 5 == 2 * A"), null)

		assertAST(parser.parse(), """
			Compound
			- ==
			-- +
			--- A
			--- 5
			-- *
			--- 2
			--- A
		""".trimIndent())
	}

	@Test
	fun logicOperationShouldHavePrecedenceOverComparison() {
		val parser = DslParser(DslLexer("A == 5 and B > C + 1"), null)

		assertAST(parser.parse(), """
			Compound
			- and
			-- ==
			--- A
			--- 5
			-- >
			--- B
			--- +
			---- C
			---- 1
		""".trimIndent())
	}
}