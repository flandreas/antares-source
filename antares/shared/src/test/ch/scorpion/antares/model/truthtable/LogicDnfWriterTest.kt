package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import kotlin.test.Test
import kotlin.test.assertEquals

class LogicDnfWriterTest {

	@Test
	fun shouldWriteLogicDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = QmcToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = StandardDnfWriter.LOGIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = A ∧ ¬B ∨ ¬A ∧ B", output)
	}

	@Test
	fun shouldOmitAndOperator() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = QmcToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = StandardDnfWriter.LOGIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = true)

		assertEquals("X = A¬B ∨ ¬AB", output)
	}
}