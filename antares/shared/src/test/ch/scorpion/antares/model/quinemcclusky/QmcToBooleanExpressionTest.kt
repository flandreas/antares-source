package ch.scorpion.antares.model.quinemcclusky

import ch.scorpion.antares.model.expression.assertAST
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.QmcToBooleanExpression
import ch.scorpion.antares.model.truthtable.TruthTable
import kotlin.test.Test

class QmcToBooleanExpressionTest {

	@Test
	fun shouldCreateBooleanExpression() {
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("X"))
		val dnf: DNF = listOf(listOf(-1, 2), listOf(1, -2))

		val node = QmcToBooleanExpression(truthTable, dnf).build()

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
}