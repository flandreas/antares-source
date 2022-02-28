package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

class TruthTableCommand(
	private val truthTableItem: TruthTableLibraryItem,
	private val row: Int,
	private val column: Int,
	private val bit: Bit
) : AbstractCommand("antares.command.truthTableCell", null), Undoable {

	private var oldValue: Bit = truthTableItem.truthTable.getValue(row, column)

	override fun execute() {
		truthTableItem.truthTable.setValue(row, column, bit)
	}

	override fun undo() {
		truthTableItem.truthTable.setValue(row, column, oldValue)
	}
}