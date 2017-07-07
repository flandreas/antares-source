package ch.scorpion.jabbah.base.swing

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

/**
 * A [TableCellRenderer] implementation for rendering enum values as a list.
 */
open class EnumRenderer<T : Enum<T>> : DefaultListCellRenderer(), TableCellRenderer {

    override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        setValue(value as T)

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
        setValue(value as T)

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

    protected open fun setValue(value: T) {
		text = value.toString()
    }
}