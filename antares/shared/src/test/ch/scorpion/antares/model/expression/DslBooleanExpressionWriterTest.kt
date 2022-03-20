package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToBooleanExpression
import ch.scorpion.antares.model.truthtable.TruthTable
import kotlin.test.Test
import kotlin.test.assertEquals

class DslBooleanExpressionWriterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldWriteDslExpression() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = DslBooleanExpressionWriter()
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = A and not B or not A and B", output)
	}
}