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
		assertEquals(
			"X = A and not B or not A and B",
			createExpression(listOf("A", "B"), listOf("X")))
	}

	@Test
	fun shouldWriteNegatedOutput() {
		assertEquals(
			"'!X' = A and not B or not A and B",
			createExpression(listOf("A", "B"), listOf("!X")))
	}

	@Test
	fun shouldWriteMultiCharacterNegatedOutput() {
		assertEquals(
			"'!(XYZ)' = A and not B or not A and B",
			createExpression(listOf("A", "B"), listOf("!(XYZ)")))
	}

	/**
	 * Create an expression of the form "X = A and not B or not A and B".
	 */
	private fun createExpression(inputColumNames: List<String>, outputColumnNames: List<String>): String {
		val truthTable = TruthTable(inputColumnNames = inputColumNames, outputColumnNames = outputColumnNames)
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		return DslBooleanExpressionWriter()
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = false)
	}
}