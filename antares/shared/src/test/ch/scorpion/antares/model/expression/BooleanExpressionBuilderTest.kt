package ch.scorpion.antares.model.expression

import ch.scorpion.antares.model.expression.BooleanExpression.and
import ch.scorpion.antares.model.expression.BooleanExpression.const
import ch.scorpion.antares.model.expression.BooleanExpression.not
import ch.scorpion.antares.model.expression.BooleanExpression.or
import ch.scorpion.antares.model.expression.BooleanExpression.variable
import kotlin.test.Test

class BooleanExpressionBuilderTest {

	@Test
	fun shouldBuildExpression() {
		// Build sample expression "AB' + A'B + 1"

		val node =
			or(
				or(
					and(variable("A"), not(variable("B"))),
					and(not(variable("A")), variable("B"))
				),
				const(true)
			)

		assertAST(node, """
			or
			- or
			-- and
			--- A
			--- not
			---- B
			-- and
			--- not
			---- A
			--- B
			- true
		""".trimIndent()
		)
	}
}