package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import kotlin.test.Test
import kotlin.test.assertEquals

class ArithmeticDnfWriterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldWriteDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = QmcToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = StandardDnfWriter.ARITHMETIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = A * B' + A' * B", output)
	}

	@Test
	fun shouldWriteFalseConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf()
		val expression = QmcToBooleanExpression(truthTable, dnf).build()

		val output = StandardDnfWriter.ARITHMETIC
			.write(truthTable, expression, 2)

		assertEquals("X = 0", output)
	}

	@Test
	fun shouldWriteTrueConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf())
		val expression = QmcToBooleanExpression(truthTable, dnf).build()

		val output = StandardDnfWriter.ARITHMETIC
			.write(truthTable, expression, 2)

		assertEquals("X = 1", output)
	}

	@Test
	fun shouldOmitAndOperator() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = QmcToBooleanExpression(truthTable, dnf).build()

		val output = StandardDnfWriter.ARITHMETIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = true)

		assertEquals("X = AB' + A'B", output)
	}

	@Test
	fun shouldNotOmitAndOperatorWithMultiCharVariableNames() {
		val truthTable = TruthTable(inputColumnNames = listOf("IN", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = QmcToBooleanExpression(truthTable, dnf).build()

		val output = StandardDnfWriter.ARITHMETIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = true)

		assertEquals("X = IN * B' + IN' * B", output)
	}

	@Test
	fun shouldApplyAddParenthesis() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = QmcToBooleanExpression(truthTable, dnf, andParenthesis = true).build()

		val output = StandardDnfWriter.ARITHMETIC
			.write(truthTable, expression, 2)

		assertEquals("X = (AB') + (A'B)", output)
	}
}