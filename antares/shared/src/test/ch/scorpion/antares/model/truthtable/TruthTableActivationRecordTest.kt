package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.base.dsl.CodeLocation
import ch.scorpion.jabbah.base.dsl.Token
import ch.scorpion.jabbah.base.dsl.TokenType
import ch.scorpion.jabbah.base.dsl.Variable
import kotlin.test.Test
import kotlin.test.assertEquals

class TruthTableActivationRecordTest {

	@Test
	fun shouldGetValue() {
		val truthTable = TruthTable("Test", listOf("A", "B"), listOf("X"))
		val ar = TruthTableActivationRecord(truthTable)

		assertRowValue(ar, 0, "A", false)
		assertRowValue(ar, 1, "A", false)
		assertRowValue(ar, 2, "A", true)
		assertRowValue(ar, 3, "A", true)

		assertRowValue(ar, 0, "B", false)
		assertRowValue(ar, 1, "B", true)
		assertRowValue(ar, 2, "B", false)
		assertRowValue(ar, 3, "B", true)

	}

	private fun assertRowValue(ar: TruthTableActivationRecord, row: Int, name: String, value: Boolean) {
		ar.currentRow = row
		val variable = Variable(CodeLocation(0, 0, 0), Token(TokenType.ID, name))
		assertEquals(value, ar.getValue(variable))
	}
}