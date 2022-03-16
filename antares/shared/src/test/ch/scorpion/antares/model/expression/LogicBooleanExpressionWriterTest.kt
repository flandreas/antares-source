package ch.scorpion.antares.model.expression

import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToBooleanExpression
import ch.scorpion.antares.model.truthtable.TruthTable
import kotlin.test.Test
import kotlin.test.assertEquals

class LogicBooleanExpressionWriterTest {

	@Test
	fun shouldWriteLogicDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = StandardBooleanExpressionWriter.LOGIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = A ∧ ¬B ∨ ¬A ∧ B", output)
	}

	@Test
	fun shouldOmitAndOperator() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = StandardBooleanExpressionWriter.LOGIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = true)

		assertEquals("X = A¬B ∨ ¬AB", output)
	}

	@Test
	fun shouldWriteInvertedOutput() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("!OUT"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = StandardBooleanExpressionWriter.LOGIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = false)

		assertEquals("¬OUT = A ∧ ¬B ∨ ¬A ∧ B", output)
	}
}