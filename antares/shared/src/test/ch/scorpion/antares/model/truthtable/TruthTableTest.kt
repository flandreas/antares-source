package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.*

class TruthTableTest {

	init {
		AntaresTestRule.configure()
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
		truthTable.setValue(1, 2, Error)

		val clone = StorableCloner.clone(truthTable)

		assertEquals(True, clone.getValue(0, 2))
		assertEquals(Error, clone.getValue(1, 2))
		assertEquals(False, clone.getValue(2, 2))
		assertEquals(False, clone.getValue(3, 2))
	}

	@Test
	fun shouldCreateMinTerm() {
		val truthTable = create2to1TruthTable()

		assertEquals(3, truthTable.getMinTerm(0))
		assertEquals(2, truthTable.getMinTerm(1))
		assertEquals(1, truthTable.getMinTerm(2))
		assertEquals(0, truthTable.getMinTerm(3))
	}

	@Test
	fun shouldGetMinTerms() {
		val truthTable = create2to1TruthTable()
		truthTable.setValue(0, 2, True)
		truthTable.setValue(1, 2, Error)
		truthTable.setValue(2, 2, False)
		truthTable.setValue(3, 2, True)

		val midTerms = truthTable.getMinTerms(2)

		assertEquals(2, midTerms.size)
		assertTrue(midTerms.contains(3)) // Values 00 inverted
		assertTrue(midTerms.contains(0)) // Values 11 inverted
	}

	@Test
	fun shouldGetNegatedOutputColumnInfo() {
		val truthTable = TruthTable("Test", listOf("A", "B"), listOf("!OUT"))

		val info = truthTable.getOutputColumnInfo(2)

		assertEquals("OUT", info.plainName)
		assertTrue(info.isNegated)
	}

	@Test
	fun shouldGetNegatedParenthesesOutputColumnInfo() {
		val truthTable = TruthTable("Test", listOf("A", "B"), listOf("!(OUT)"))

		val info = truthTable.getOutputColumnInfo(2)

		assertEquals("OUT", info.plainName)
		assertTrue(info.isNegated)
	}

	@Test
	fun shouldGetNonNegatedOutputColumnInfo() {
		val truthTable = TruthTable("Test", listOf("A", "B"), listOf("OUT"))

		val info = truthTable.getOutputColumnInfo(2)

		assertEquals("OUT", info.plainName)
		assertFalse(info.isNegated)
	}

	@Test
	fun shouldClone() {
		val truthTable = create2to1TruthTable()
		truthTable.setValue(0, 2, True)
		truthTable.setValue(1, 2, Error)
		truthTable.setValue(2, 2, False)
		truthTable.setValue(3, 2, True)

		val clone: TruthTable = truthTable.doClone()

		assertEquals(3, clone.columnCount)
		assertEquals(4, clone.rowsCount)
		assertEquals("A", clone.getColumnName(0))
		assertEquals("B", clone.getColumnName(1))
		assertEquals("O", clone.getColumnName(2))
		assertEquals(True, clone.getValue(0, 2))
		assertEquals(Error, clone.getValue(1, 2))
		assertEquals(False, clone.getValue(2, 2))
		assertEquals(True, clone.getValue(3, 2))
	}

	private fun create2to1TruthTable(): TruthTable =
		TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("O"))
}