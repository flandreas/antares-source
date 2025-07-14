package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.command.AbstractCommand

class TruthTableImportCSVCommand(
    private val reference: TruthTableReference,
    private val oldValue: TruthTable,
    private val newValue: TruthTable
) : AbstractCommand("antares.truthTable.csv.import.name"), Undoable {

    override fun execute() {
        reference.truthTable.applyValues(newValue)
    }

    override fun undo() {
        reference.truthTable.applyValues(oldValue)
    }
}