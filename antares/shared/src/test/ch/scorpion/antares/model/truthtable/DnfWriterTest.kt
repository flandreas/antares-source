package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import kotlin.test.Test
import kotlin.test.assertEquals

class DnfWriterTest {

	@Test
	fun shouldWriteDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		assertEquals("X = A * B' + A' * B",
			DnfWriter(
				truthTable,
				QmcToBooleanExpression(truthTable, dnf).build()
			).write(2))
	}

	@Test
	fun shouldWriteFalseConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf()

		assertEquals("X = 0",
			DnfWriter(
				truthTable,
				QmcToBooleanExpression(truthTable, dnf).build()
			).write(2))
	}

	@Test
	fun shouldWriteTrueConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf())

		assertEquals("X = 1",
			DnfWriter(
				truthTable,
				QmcToBooleanExpression(truthTable, dnf).build()
			).write(2))
	}

}