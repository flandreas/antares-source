package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.TranslatableTextPanel
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.Color
import java.awt.Component
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class TranslatableTextPropertyRenderer : DefaultTableCellRenderer() {

	override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		if (value is String) {
			setValue(value)
		} else if (value is TranslatableText) {
			setValue(value.getTranslation())
		}

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
}

class TranslatableTextPropertyEditor : AbstractPropertyEditor() {

	companion object {
		private val LOG by logger(TranslatableTextPropertyEditor::class)

		// Holds the single [JDialog] instance across all [TranslatableTextPropertyEditor] instances.
		private val dialog: JDialog? = null
	}

	private val textField = JTextField()
	private val button = JButton()
	private var text: TranslatableText = TranslatableText()

	init {
		buildUI()
	}

	/** ---- [AbstractPropertyEditor] */

	override fun getValue(): Any {
		return text.withTranslation(textField.text)
	}

	override fun setValue(value: Any?) {
		text = value as TranslatableText
		textField.text = text.getTranslation()
	}

	private fun buildUI() {
		val panel = JPanel()
		panel.background = Color.WHITE
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		panel.add(textField)

		button.alignmentY = Component.TOP_ALIGNMENT
		button.icon = ImageIcon(TextPropertyEditor::class.java.getResource("/img/openInPopup-16.png"))
		button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
		button.toolTipText = Translations.getString("edit.action.editText.tooltip")
		button.addActionListener { showDialog() }
		panel.add(button)

		editor = panel
	}

	private fun showDialog() {
		val newText = TranslatableTextPanel.showAsDialog(
			title = "Text",
			text = text
		)
		newText?.let { value = it }
	}
}