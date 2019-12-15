package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.ColorIcon
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class PredefinedColorRenderer : DefaultListCellRenderer(), TableCellRenderer {

	private val colorIcon = ColorIcon()

	override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		setValue(value as PredefinedColor?)

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
		setValue(value as PredefinedColor?)
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

	private fun setValue(color: PredefinedColor?) {
		if (color == null) {
			icon = null
			text = Translations.getString("edit.style.property.fromStyle.name")
		} else {
			colorIcon.backgroundColor = Graphics2DJvm.toAwtColor(color.color.backgroundColor)
			colorIcon.foregroundColor = Graphics2DJvm.toAwtColor(color.color.foregroundColor)
			text = color.description
			icon = colorIcon
		}
	}
}

class PredefinedColorEditor(colorProvider: PredefinedColorProvider) : ComboBoxPropertyEditor() {
	init {
		val list = mutableListOf<PredefinedColor?>(null)
		list.addAll(colorProvider.provideAll())
		setAvailableValues(list.toTypedArray())
		(editor as JComboBox<*>).renderer = PredefinedColorRenderer()
	}
}

