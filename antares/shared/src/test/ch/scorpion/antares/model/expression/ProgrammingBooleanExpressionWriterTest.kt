package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToBooleanExpression
import ch.scorpion.antares.model.truthtable.TruthTable
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgrammingBooleanExpressionWriterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldWriteDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = true).build()

		val output = StandardBooleanExpressionWriter.PROGRAMMING
			.write(truthTable, expression,2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = (A && !B) || (!A && B)", output)
	}
}