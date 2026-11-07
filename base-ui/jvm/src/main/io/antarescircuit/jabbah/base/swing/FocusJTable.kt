package io.antarescircuit.jabbah.base.swing

import javax.swing.JTable
import javax.swing.table.TableModel

/** A editable [JTable] implementation that request focus on cell editors.*/
class FocusJTable(model: TableModel) : JTable(model) {

	override fun changeSelection(rowIndex: Int, columnIndex: Int, toggle: Boolean, extend: Boolean) {
		super.changeSelection(rowIndex, columnIndex, toggle, extend)
		if (editCellAt(rowIndex, columnIndex)) {
			editorComponent.requestFocusInWindow()
		}
	}
}