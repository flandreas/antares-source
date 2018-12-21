package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.TranslatableTextPanel
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.Color
import java.awt.Component
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.JTextComponent

class TranslatableTextPropertyRenderer(
	multiline: (TranslatableText) -> Boolean = {_ -> false}
) : DefaultTableCellRenderer() {

	private val textComponent: JTextComponent?

	init {
		if (multiline.invoke(TranslatableText())) {
			textComponent = JTextArea()
			textComponent.rows = 4
			textComponent.lineWrap = true
			textComponent.isEditable = false
		} else {
			textComponent = null
		}
	}

	override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		if (textComponent != null) {
			if (value is String) {
				textComponent.text = value

			} else if (value is TranslatableText) {
				textComponent.text = value.getOptionalTranslation()
			}

			if (isSelected) {
				textComponent.foreground = table.selectionForeground
				textComponent.background = table.selectionBackground
			} else {
				textComponent.foreground = table.foreground
				textComponent.background = table.background
			}
			textComponent.font = table.font
			return textComponent
		} else {
			if (value is String) {
				text = value

			} else if (value is TranslatableText) {
				text = value.getOptionalTranslation()
			}
			return this
		}
	}
}

class TranslatableTextPropertyEditor(
	private val propertyName: String,
	private val multiline: (TranslatableText) -> Boolean = {_ -> false}
) : AbstractPropertyEditor() {

	private val textComponent: JTextComponent
	private val button = JButton()
	private var text: TranslatableText = TranslatableText()

	init {
		if (multiline.invoke(text)) {
			textComponent = JTextArea()
			textComponent.rows = 4
			textComponent.lineWrap = true
			textComponent.isEditable = true
			textComponent.border = null
		} else {
			textComponent = JTextField()
		}
		buildUI()
	}

	/** ---- [AbstractPropertyEditor] */

	override fun getValue(): Any {
		return if (StringUtils.isBlank(textComponent.text)) {
			text
		} else {
			text.withTranslation(textComponent.text)
		}
	}

	override fun setValue(value: Any?) {
		text = value as TranslatableText
		textComponent.text = text.getOptionalTranslation()
	}

	private fun buildUI() {
		val panel = JPanel()
		panel.background = Color.WHITE
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		if (textComponent is JTextArea) {
			val scrollPane = JScrollPane(textComponent)
			scrollPane.alignmentY = Component.TOP_ALIGNMENT
			scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
			scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
			panel.add(scrollPane)
		} else {
			panel.add(textComponent)
		}

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
			title = propertyName,
			text = text,
			textFieldRows = if (multiline.invoke(text)) 4 else 1
		)
		newText?.let { value = it }
	}
}