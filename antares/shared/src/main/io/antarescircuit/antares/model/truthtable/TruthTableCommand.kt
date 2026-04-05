package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.jabbah.edit.Undoable
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.command.AbstractCommand

/**
 * A [Command] for setting an individual [Bit] in an output cell of a [TruthTable].
 */
class TruthTableCommand(
	private val ref: TruthTableReference,
	private val row: Int,
	private val column: Int,
	private val bit: Bit
) : AbstractCommand("antares.command.truthTableCell", null), Undoable {

	private var oldValue: Bit = ref.truthTable.getValue(row, column)

	override fun execute() {
		ref.truthTable.setValue(row, column, bit)
	}

	override fun undo() {
		ref.truthTable.setValue(row, column, oldValue)
	}
}