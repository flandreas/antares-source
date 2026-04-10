package io.antarescircuit.jabbah.base.swing

import java.awt.Component
import javax.swing.JList
import javax.swing.JTable
import javax.swing.ListCellRenderer
import javax.swing.table.DefaultTableCellRenderer

open class ToStringRenderer<T : Any>(
	private val nullText: String = ""
) : DefaultTableCellRenderer(), ListCellRenderer<T> {

	override fun getListCellRendererComponent(list: JList<out T>, value: T?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		@Suppress("UNCHECKED_CAST")
		setValue(value)

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
		text = value?.toString() ?: nullText
	}
}