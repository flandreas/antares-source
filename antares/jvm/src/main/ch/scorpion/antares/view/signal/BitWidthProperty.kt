package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class BitWidthRenderer : DefaultListCellRenderer(), TableCellRenderer {

	override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		setValue(value as BitWidth?)

		if (isSelected) {
			foreground = list.selectionForeground
			background = list.selectionBackground
		} else {
			foreground = list.foreground
			background = list.background
		}
		font = list.font
		return this
	}

	override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		setValue(value as BitWidth?)

		if (isSelected) {
			foreground = table.selectionForeground
			background = table.selectionBackground
		} else {
			foreground = table.foreground
			background = table.background
		}
		font = table.font
		return this
	}

	private fun setValue(bitWidth: BitWidth?) {
		text = bitWidth?.toString() ?: ""
	}
}

class BitWidthEditor(filter: (BitWidth) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
	init {
		val list = BitWidth.PREDEFINED.filter { filter(it) }.toMutableList()
		list.add(BitWidthExpression(""))
		setAvailableValues(list.toTypedArray())
		(editor as JComboBox<*>).renderer = BitWidthRenderer()
	}
}