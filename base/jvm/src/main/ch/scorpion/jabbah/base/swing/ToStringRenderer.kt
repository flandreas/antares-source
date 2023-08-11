package ch.scorpion.jabbah.base.swing

import java.awt.Component
import javax.swing.JList
import javax.swing.JTable
import javax.swing.ListCellRenderer
import javax.swing.table.DefaultTableCellRenderer

open class ToStringRenderer<T : Any> : DefaultTableCellRenderer(), ListCellRenderer<Any> {

	override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		@Suppress("UNCHECKED_CAST")
		setValue(value as T?)

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
		@Suppress("UNCHECKED_CAST")
		setValue(value as T?)

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

	override fun setValue(value: Any?) {
		text = value?.toString() ?: ""
	}
}