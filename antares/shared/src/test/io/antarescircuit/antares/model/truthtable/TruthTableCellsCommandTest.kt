package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.Bit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TruthTableCellsCommandTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldUndoAndRedoAnEntireOutputBlock() {
		val table = TruthTable("Test", listOf("A"), listOf("O", "P"))
		table.setValue(0, 1, Bit.Error)
		val ref = TruthTableReference({ table })
		try {
			val command = TruthTableCellsCommand(ref, listOf(
				TruthTableCellChange(0, 1, Bit.True),
				TruthTableCellChange(0, 2, Bit.Error),
				TruthTableCellChange(1, 1, Bit.True),
			))
			command.execute()
			assertEquals(Bit.True, table.getValue(0, 1))
			assertEquals(Bit.Error, table.getValue(0, 2))
			assertEquals(Bit.True, table.getValue(1, 1))
			command.undo()
			assertEquals(Bit.Error, table.getValue(0, 1))
			assertEquals(Bit.False, table.getValue(0, 2))
			assertEquals(Bit.False, table.getValue(1, 1))
			command.execute()
			assertEquals(Bit.True, table.getValue(1, 1))
		} finally {
			ref.dispose()
		}
	}

	@Test
	fun shouldRejectAnInvalidBlockBeforeChangingAnyCells() {
		val table = TruthTable("Test", listOf("A"), listOf("O"))
		val ref = TruthTableReference({ table })
		try {
			for (invalid in listOf(TruthTableCellChange(0, 0, Bit.True), TruthTableCellChange(2, 1, Bit.True))) {
				assertFailsWith<IllegalArgumentException> {
					TruthTableCellsCommand(ref, listOf(TruthTableCellChange(0, 1, Bit.True), invalid))
				}
				assertEquals(Bit.False, table.getValue(0, 1))
			}
		} finally {
			ref.dispose()
		}
	}
}
