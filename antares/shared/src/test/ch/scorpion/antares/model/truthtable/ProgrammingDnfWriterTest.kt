package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgrammingDnfWriterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldWriteDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = QmcToBooleanExpression(truthTable, dnf, andParenthesis = true).build()

		val output = StandardDnfWriter.PROGRAMMING
			.write(truthTable, expression,2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = (A && !B) || (!A && B)", output)
	}
}