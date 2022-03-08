package ch.scorpion.antares.model.expression

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToBooleanExpression
import ch.scorpion.antares.model.truthtable.TruthTable
import kotlin.test.Test
import kotlin.test.assertEquals

class ArithmeticBooleanExpressionWriterTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldWriteDnf() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		val output = StandardBooleanExpressionWriter.ARITHMETIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = false)

		assertEquals("X = A * B' + A' * B", output)
	}

	@Test
	fun shouldWriteFalseConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf()
		val expression = DnfToBooleanExpression(truthTable, dnf).build()

		val output = StandardBooleanExpressionWriter.ARITHMETIC
			.write(truthTable, expression, 2)

		assertEquals("X = 0", output)
	}

	@Test
	fun shouldWriteTrueConstant() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf())
		val expression = DnfToBooleanExpression(truthTable, dnf).build()

		val output = StandardBooleanExpressionWriter.ARITHMETIC
			.write(truthTable, expression, 2)

		assertEquals("X = 1", output)
	}

	@Test
	fun shouldOmitAndOperator() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf).build()

		val output = StandardBooleanExpressionWriter.ARITHMETIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = true)

		assertEquals("X = AB' + A'B", output)
	}

	@Test
	fun shouldNotOmitAndOperatorWithMultiCharVariableNames() {
		val truthTable = TruthTable(inputColumnNames = listOf("IN", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf).build()

		val output = StandardBooleanExpressionWriter.ARITHMETIC
			.write(truthTable, expression, 2, omitAndForSingleCharacterVariables = true)

		assertEquals("X = IN * B' + IN' * B", output)
	}

	@Test
	fun shouldApplyAddParenthesis() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))
		val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = true).build()

		val output = StandardBooleanExpressionWriter.ARITHMETIC
			.write(truthTable, expression, 2)

		assertEquals("X = (AB') + (A'B)", output)
	}
}