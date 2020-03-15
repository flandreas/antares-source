package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.TranslatableTextPanel
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.JTextComponent

class TranslatableTextPropertyRenderer(
	multiline: (TranslatableText) -> Boolean = { _ -> false }
) : DefaultTableCellRenderer() {

	private val textComponent: JTextComponent?

	init {
		if (multiline.invoke(TranslatableText())) {
			textComponent = JTextArea()
			textComponent.rows = 4
			textComponent.lineWrap = true
			textComponent.wrapStyleWord = true
			textComponent.isEnabled = false
		} else {
			textComponent = JTextField()
			textComponent.preferredSize = Dimension(100, 22)
			textComponent.border = null
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
	private val multiline: (TranslatableText) -> Boolean = { _ -> false },
	rows: Int = 4
) : AbstractPropertyEditor() {

	private val textComponent: JTextComponent
	private val button = JButton()
	private var text: TranslatableText = TranslatableText()

	init {
		if (multiline.invoke(text)) {
			textComponent = JTextArea()
			textComponent.rows = rows
			textComponent.lineWrap = true
			textComponent.wrapStyleWord = true
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
			text.withoutTranslation()
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
			val scroll = UiUtil.decorateTextArea(textComponent)
			textComponent.columns = 20
			scroll.border = null
			textComponent.border = UIManager.getBorder("TextField.border")
			panel.add(scroll)
		} else {
			textComponent.alignmentY = Component.CENTER_ALIGNMENT
			panel.add(textComponent)
		}

		panel.border = textComponent.border
		textComponent.border = null

		button.alignmentY = Component.CENTER_ALIGNMENT
		button.icon = ImageIcon(TextPropertyEditor::class.java.getResource("/img/translation-16.png"))
		button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
		button.toolTipText = Translations.getString("edit.action.translateText.tooltip")
		button.addActionListener { showDialog() }
		panel.add(button)

		editor = panel
	}

	private fun showDialog() {
		val newText = TranslatableTextPanel.showAsDialog(
			parent = SwingUtilities.getWindowAncestor(button),
			title = propertyName,
			text = text,
			textFieldRows = if (multiline.invoke(text)) 8 else 1,
			textFieldColumns = 40
		)
		newText?.let { value = it }
	}
}