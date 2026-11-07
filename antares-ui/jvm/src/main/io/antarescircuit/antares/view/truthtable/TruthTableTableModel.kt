package io.antarescircuit.antares.view.truthtable

import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.expression.StandardBooleanExpressionWriter
import io.antarescircuit.antares.model.truthtable.TruthTableListener
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import javax.swing.table.AbstractTableModel

class TruthTableTableModel(
	private val ref: TruthTableReference,
	private val editable: Boolean = true
) : AbstractTableModel() {

	private val listener = TruthTableListener { event ->
		fireTableCellUpdated(event.row, event.column)
	}

	/** Translates standard Antares port name negations into texts depending on [BooleanExpressionNotation].*/
	private val formattedOutputNames =
		(ref.truthTable.inputColumnCount until ref.truthTable.columnCount)
			.map { StandardBooleanExpressionWriter.ofPropertiesNotation().getOutput(ref.truthTable, it) }

	init {
		ref.addDataListener(listener)
	}

	fun dispose() {
		ref.removeDataListener(listener)
	}

	override fun getColumnName(column: Int): String =
		if (column >= ref.truthTable.inputColumnCount) {
			formattedOutputNames[column - ref.truthTable.inputColumnCount]
		} else {
			ref.truthTable.getColumnName(column)
		}

	override fun getRowCount(): Int = ref.truthTable.rowsCount

	override fun getColumnCount(): Int = ref.truthTable.inputColumnCount + ref.truthTable.outputColumnCount

	override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
		ref.truthTable.getValue(rowIndex, columnIndex)

	override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
		editable && columnIndex >= ref.truthTable.inputColumnCount
}