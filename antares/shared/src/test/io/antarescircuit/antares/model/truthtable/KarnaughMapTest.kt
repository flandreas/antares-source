package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.Bit
import kotlin.test.Test
import kotlin.test.assertEquals

class KarnaughMapTest {

	private val service = TruthTableService()

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldMapEveryCellToADistinctTruthTableRow() {
		for (n in 1..KarnaughMap.MAX_VARIABLES) {
			val map = KarnaughMap(n)
			val rows = map.cells.map { map.truthTableRow(it) }
			assertEquals((0 until (1 shl n)).toList(), rows.sorted(), "$n variables")
			map.cells.forEach { assertEquals(it, map.cellOf(map.truthTableRow(it))) }
		}
	}

	@Test
	fun shouldMakeNeighboringCellsDifferInExactlyOneVariableIncludingWraparound() {
		for (n in 2..KarnaughMap.MAX_VARIABLES) {
			val map = KarnaughMap(n)
			for (cell in map.cells) {
				val neighbors = listOfNotNull(
					cell.copy(row = (cell.row + 1) % map.rowCount).takeIf { map.rowCount > 1 },
					cell.copy(column = (cell.column + 1) % map.columnCount),
					cell.copy(submap = (cell.submap + 1) % map.submapCount).takeIf { map.submapCount > 1 },
				)
				for (neighbor in neighbors) {
					val difference = map.truthTableRow(cell) xor map.truthTableRow(neighbor)
					assertEquals(1, difference.countOneBits(), "$n variables: $cell vs. $neighbor")
				}
			}
		}
	}

	@Test
	fun shouldSplitBNotDNotIntoTheFourCorners() {
		val table = TruthTable("T", listOf("A", "B", "C", "D"), listOf("O"))
		(0 until table.rowsCount).filter { it and 0b0101 == 0 }.forEach { table.setValue(it, 4, Bit.True) }

		val map = KarnaughMap(4)
		val rectangles = service.generateDnf(table, 4).single().let { map.rectangles(it) }

		assertEquals(
			setOf(
				KarnaughRectangle(0, 0, 0, 1, 1),
				KarnaughRectangle(0, 0, 3, 1, 1),
				KarnaughRectangle(0, 3, 0, 1, 1),
				KarnaughRectangle(0, 3, 3, 1, 1),
			),
			rectangles.toSet(),
		)
	}

	@Test
	fun shouldCoverTwoNeighboringCellsForANotBNotInAThreeVariableMap() {
		val table = TruthTable("T", listOf("A", "B", "C"), listOf("O"))
		table.setColumnValues(3, Bit.True, Bit.True)

		val term = service.generateDnf(table, 3).single()

		assertEquals(listOf(KarnaughRectangle(0, 0, 0, 1, 2)), KarnaughMap(3).rectangles(term))
	}

	@Test
	fun shouldCoverTheWholeMapForConstantOne() {
		val table = TruthTable("T", listOf("A", "B", "C"), listOf("O"))
		table.setColumnValue(3, Bit.True)

		val term = service.generateDnf(table, 3).single()

		assertEquals(listOf(KarnaughRectangle(0, 0, 0, 2, 4)), KarnaughMap(3).rectangles(term))
	}

	@Test
	fun shouldRepeatATermIndependentOfTheSubmapVariableInEachSubmap() {
		// E is the last of five variables and selects the two middle columns of each 4x4 submap
		assertEquals(
			listOf(KarnaughRectangle(0, 0, 1, 4, 2), KarnaughRectangle(1, 0, 1, 4, 2)),
			KarnaughMap(5).rectangles(listOf(-5)),
		)
	}
}
