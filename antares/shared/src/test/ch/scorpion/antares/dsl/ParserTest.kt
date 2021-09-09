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
}