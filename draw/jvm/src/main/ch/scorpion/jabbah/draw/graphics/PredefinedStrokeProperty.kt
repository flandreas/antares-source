package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.Translations
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.*
import javax.swing.table.TableCellRenderer

class PredefinedStrokeRenderer : DefaultListCellRenderer(), TableCellRenderer {

	private val strokeIcon = StrokeIcon()

	override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		setValue(value as PredefinedStroke?)

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
		setValue(value as PredefinedStroke?)
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

	private fun setValue(stroke: PredefinedStroke?) {
		if (stroke == null) {
			icon = null
			text = Translations.getString("edit.style.property.fromStyle.name")
		} else {
			strokeIcon.stroke = Graphics2DJvm.toAwtStroke(stroke.stroke)
			text = null
			icon = strokeIcon
		}
	}
}

class PredefinedStrokeEditor(strokeProvider: PredefinedStrokeProvider) : ComboBoxPropertyEditor() {
	init {
		val list = mutableListOf<PredefinedStroke?>(null)
		list.addAll(strokeProvider.provideAll())
		setAvailableValues(list.toTypedArray())
		(editor as JComboBox<*>).renderer = PredefinedStrokeRenderer()
	}
}