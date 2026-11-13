package io.antarescircuit.antares.view.truthtable

import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Undoable
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals

class TruthTableTableViewTest {

	@Test
	fun shouldFillASelectionAsOneUndoableEditWithoutChangingInputs() {
		SwingUtilities.invokeAndWait {
			AntaresTestRule.configure()
			val model = TruthTable("Test", listOf("A"), listOf("O", "P"))
			val ref = TruthTableReference({ model })
			try {
				val commands = mutableListOf<Command>()
				val captured = Capture.slot<Command>()
				val manager = mock<CommandManager>()
				every { manager.execute(capture(captured)) } calls {
					captured.get().let { commands.add(it); it.execute() }
				}
				val view = TruthTableTableView(ref, manager)
				view.table.setRowSelectionInterval(0, 1)
				view.table.setColumnSelectionInterval(0, 2)
				view.table.actionMap.get("ActionX").actionPerformed(null)
				assertEquals(1, commands.size)
				assertEquals(listOf(Bit.Error, Bit.Error), model.getColumnValues(1))
				assertEquals(listOf(Bit.Error, Bit.Error), model.getColumnValues(2))
				assertEquals(listOf(Bit.False, Bit.True), model.getColumnValues(0))
				(commands.single() as Undoable).undo()
				assertEquals(listOf(Bit.False, Bit.False), model.getColumnValues(1))
			} finally {
				ref.dispose()
			}
		}
	}

	@Test
	fun shouldEditTheCorrectOutputAndAdvanceAfterColumnsAreReordered() {
		SwingUtilities.invokeAndWait {
			AntaresTestRule.configure()
			val model = TruthTable("Test", listOf("A"), listOf("O", "P"))
			val ref = TruthTableReference({ model })
			try {
				val manager = mock<CommandManager>()
				val captured = Capture.slot<Command>()
				every { manager.execute(capture(captured)) } calls { captured.get().execute() }
				val view = TruthTableTableView(ref, manager)
				view.table.moveColumn(2, 0)
				view.table.changeSelection(0, 0, false, false)
				view.table.actionMap.get("Action1").actionPerformed(null)
				assertEquals(Bit.True, model.getValue(0, 2))
				assertEquals(Bit.False, model.getValue(0, 0))
				assertEquals(2, view.table.selectedColumn)
				assertEquals(0, view.table.selectedRow)
			} finally {
				ref.dispose()
			}
		}
	}
}
