package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.quinemccluskey.Literal
import kotlin.test.Test
import kotlin.test.assertEquals

class DnfWriterTest {

	@Test
	fun shouldWriteDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: List<List<Literal>> = listOf(listOf(-1, 2), listOf(1, -2))

		assertEquals("X = AB' + A'B", DnfWriter(truthTable, dnf).write(2))
	}

	@Test
	fun shouldWriteFalseConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: List<List<Literal>> = listOf()

		assertEquals("X = 0", DnfWriter(truthTable, dnf).write(2))
	}

	@Test
	fun shouldWriteTrueConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: List<List<Literal>> = listOf(listOf())

		assertEquals("X = 1", DnfWriter(truthTable, dnf).write(2))
	}

}