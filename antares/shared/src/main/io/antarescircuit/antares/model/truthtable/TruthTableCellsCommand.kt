package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.command.AbstractCommand

data class TruthTableCellChange(val row: Int, val column: Int, val value: Bit)

/** Applies a validated set of output edits as one undo step. */
class TruthTableCellsCommand(
	private val ref: TruthTableReference,
	changes: List<TruthTableCellChange>,
) : AbstractCommand("antares.command.truthTableCells"), Undoable {

	private val changes = changes.toList()
	private val previousValues: List<TruthTableCellChange>

	init {
		val table = ref.truthTable
		require(changes.all {
			it.row in 0 until table.rowsCount && it.column in table.inputColumnCount until table.columnCount
		}) { "Only existing truth table outputs can be edited" }
		require(changes.map { it.row to it.column }.distinct().size == changes.size) { "Duplicate cell edits" }
		previousValues = changes.map { it.copy(value = table.getValue(it.row, it.column)) }
	}

	override fun execute() {
		changes.forEach { ref.truthTable.setValue(it.row, it.column, it.value) }
	}

	override fun undo() {
		previousValues.forEach { ref.truthTable.setValue(it.row, it.column, it.value) }
	}
}
