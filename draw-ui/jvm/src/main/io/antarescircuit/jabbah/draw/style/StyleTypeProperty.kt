package io.antarescircuit.jabbah.draw.style

import io.antarescircuit.jabbah.base.Translations
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTable
import javax.swing.ListCellRenderer
import javax.swing.table.DefaultTableCellRenderer

class StyleTypeEditor(styleProvider: StyleProvider = DrawStyleModule.styleProvider) : ComboBoxPropertyEditor() {

	init {
		setAvailableValues(styleProvider.getChoosableStyleTypes().toTypedArray())
		@Suppress("UNCHECKED_CAST")
		(editor as JComboBox<StyleType>).renderer = StyleTypeRenderer()
	}
}

class StyleTypeRenderer : DefaultTableCellRenderer(), ListCellRenderer<StyleType> {

	override fun getListCellRendererComponent(
		list: JList<out StyleType>?,
		value: StyleType?,
		index: Int,
		isSelected: Boolean,
		cellHasFocus: Boolean
	): Component {
		setValue(value as StyleType)

		if (isSelected) {
			foreground = list?.selectionForeground
			background = list?.selectionBackground
		} else {
			foreground = list?.foreground
			background = list?.background
		}
		font = list?.font
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