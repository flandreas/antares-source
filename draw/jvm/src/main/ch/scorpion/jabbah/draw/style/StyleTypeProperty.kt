package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.Translations
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class StyleTypeEditor(styleProvider: StyleProvider = DrawStyleModule.styleProvider) : ComboBoxPropertyEditor() {

	init {
		setAvailableValues(styleProvider.getChoosableStyleTypes().toTypedArray())
		(editor as JComboBox<*>).renderer = StyleTypeRenderer()
	}
}

class StyleTypeRenderer : DefaultListCellRenderer(), TableCellRenderer {

	override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		setValue(value as StyleType)

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
		setValue(value as StyleType)

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

	private fun setValue(styleType: StyleType?) {
		if (styleType == null) {
			icon = null
			text = Translations.getString("draw.style.none")
		} else {
			text = styleType.description
		}
	}
}