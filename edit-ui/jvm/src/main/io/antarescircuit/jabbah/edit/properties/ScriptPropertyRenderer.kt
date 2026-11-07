package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class ScriptPropertyRenderer : DefaultTableCellRenderer() {

	companion object {
		private val LABEL_TEXT = Translations.getString("edit.property.script.name")

		fun getText(property: ScriptProperty): String {
			return if (property.isEmpty()) {
				""
			} else {
				LABEL_TEXT
			}
		}
	}

	override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
		label.text = getText(value as ScriptProperty)
		return label
	}
}