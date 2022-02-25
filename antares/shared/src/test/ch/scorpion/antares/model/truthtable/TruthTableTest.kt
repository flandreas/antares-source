package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TruthTableTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldCreateWithColumnNames() {
		val truthTable = create2to1TruthTable()
		assertEquals(2, truthTable.inputColumnCount)
		assertEquals(1, truthTable.outputColumnCount)
		assertEquals(4, truthTable.rowsCount)
	}

	@Test
	fun shouldCreateInitialCells() {
		val truthTable = create2to1TruthTable()

		// Inputs
		assertEquals(False, truthTable.getValue(0, 0))
		assertEquals(False, truthTable.getValue(0, 1))
		assertEquals(False, truthTable.getValue(1, 0))
		assertEquals(True, truthTable.getValue(1, 1))
		assertEquals(True, truthTable.getValue(2, 0))
		assertEquals(False, truthTable.getValue(2, 1))
		assertEquals(True, truthTable.getValue(3, 0))
		assertEquals(True, truthTable.getValue(3, 1))

		// Output2
		assertEquals(False, truthTable.getValue(0, 2))
		assertEquals(False, truthTable.getValue(1, 2))
		assertEquals(False, truthTable.getValue(2, 2))
		assertEquals(False, truthTable.getValue(3, 2))
	}

	@Test
	fun shouldSetOutputCell() {
		val truthTable = create2to1TruthTable()

		truthTable.setValue(0, 2, True)

		assertEquals(True, truthTable.getValue(0, 2))
	}

	@Test
	fun shouldNotSetInputCell() {
		val truthTable = create2to1TruthTable()
		assertFailsWith(IllegalArgumentException::class) {
			truthTable.setValue(0, 0, True)
		}
	}

	@Test
	fun shouldBeStorable() {
		val truthTable = create2to1TruthTable()
		truthTable.setValue(0, 2, True)
		truthTable.setValue(1, 2, Undefined)

		val clone = StorableCloner.clone(truthTable)

		assertEquals(True, clone.getValue(0, 2))
		assertEquals(Undefined, clone.getValue(1, 2))
		assertEquals(False, clone.getValue(2, 2))
		assertEquals(False, clone.getValue(3, 2))
	}

	private fun create2to1TruthTable(): TruthTable =
		TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("O"))
}