package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.model.truthtable.TruthTable
import javax.swing.table.AbstractTableModel

class TruthTableTableModel(
	private val truthTable: TruthTable
) : AbstractTableModel() {

	override fun getColumnName(column: Int): String = truthTable.getColumnName(column)

	override fun getRowCount(): Int = truthTable.rowsCount

	override fun getColumnCount(): Int = truthTable.inputColumnCount + truthTable.outputColumnCount

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
		truthTable.getValue(rowIndex, columnIndex)

	override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
		columnIndex >= truthTable.inputColumnCount
}