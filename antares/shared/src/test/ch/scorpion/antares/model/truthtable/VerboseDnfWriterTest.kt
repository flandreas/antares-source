package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import kotlin.test.Test
import kotlin.test.assertEquals

class VerboseDnfWriterTest {

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

		val output = StandardDnfWriter.VERBOSE
			.write(truthTable, expression,2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = (A AND NOT B) OR (NOT A AND B)", output)
	}
}