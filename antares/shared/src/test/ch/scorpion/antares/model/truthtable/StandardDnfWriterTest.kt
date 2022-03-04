package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import kotlin.test.Test
import kotlin.test.assertEquals

class StandardDnfWriterTest {

	@Test
	fun shouldWriteDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		assertEquals("X = A * B' + A' * B",
			StandardDnfWriter.ARITHMETIC
				.write(truthTable, QmcToBooleanExpression(truthTable, dnf).build(), 2))
	}

	@Test
	fun shouldWriteFalseConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf()

		assertEquals("X = 0",
			StandardDnfWriter.ARITHMETIC
				.write(truthTable, QmcToBooleanExpression(truthTable, dnf).build(), 2))
	}

	@Test
	fun shouldWriteTrueConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf())

		assertEquals("X = 1",
			StandardDnfWriter.ARITHMETIC
				.write(truthTable, QmcToBooleanExpression(truthTable, dnf).build(), 2))
	}

	@Test
	fun shouldWriteLogicDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		assertEquals("X = A ∧ ¬B ∨ ¬A ∧ B",
			StandardDnfWriter.LOGIC
				.write(truthTable, QmcToBooleanExpression(truthTable, dnf).build(), 2))
	}
}