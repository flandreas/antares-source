package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.base.dsl.BaseTokenType
import ch.scorpion.jabbah.base.dsl.Variable
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.parser.Token
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
		val variable = Variable(TextLocation(0, 0, 0), Token(BaseTokenType.ID, name))
		assertEquals(value, ar.getValue(variable))
	}
}