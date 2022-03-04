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

		assertEquals("X = A ∧ ¬B ∨ ¬A ∧ B",
			StandardDnfWriter.LOGIC
				.write(truthTable, QmcToBooleanExpression(truthTable, dnf).build(), 2))
	}

	@Test
	fun shouldOmitAndOperator() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		assertEquals("X = A¬B ∨ ¬AB",
			StandardDnfWriter.LOGIC
				.write(truthTable, QmcToBooleanExpression(truthTable, dnf).build(), 2, omitAndForSingleCharacterVariables = true))
	}
}