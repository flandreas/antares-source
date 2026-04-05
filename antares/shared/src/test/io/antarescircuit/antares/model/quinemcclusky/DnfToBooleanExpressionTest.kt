package io.antarescircuit.antares.model.quinemcclusky

import io.antarescircuit.antares.model.expression.assertAST
import io.antarescircuit.antares.model.quinemccluskey.DNF
import io.antarescircuit.antares.model.quinemccluskey.DnfToBooleanExpression
import io.antarescircuit.antares.model.truthtable.TruthTable
import kotlin.test.Test

class DnfToBooleanExpressionTest {

	@Test
	fun shouldCreateBooleanExpression() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		val node = DnfToBooleanExpression(truthTable, dnf).build()

		assertAST(node, """
			or
			- and
			-- A
			-- not
			--- B
			- and
			-- not
			--- A
			-- B
		""".trimIndent())
	}

	@Test
	fun shouldApplyParenthesis() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		val node = DnfToBooleanExpression(truthTable, dnf, andParenthesis = true).build()

		assertAST(node, """
			or
			- Compound
			-- and
			--- A
			--- not
			---- B
			- Compound
			-- and
			--- not
			---- A
			--- B
		""".trimIndent())
	}

	@Test
	fun shouldCreateConstantTrue() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf()) // An empty list means "constant true"

		val node = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		assertAST(node, """
			true
		""".trimIndent())
	}

	@Test
	fun shouldCreateConstantFalse() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf() // Nothing means "constant false"

		val node = DnfToBooleanExpression(truthTable, dnf, andParenthesis = false).build()

		assertAST(node, """
			false
		""".trimIndent())
	}

}