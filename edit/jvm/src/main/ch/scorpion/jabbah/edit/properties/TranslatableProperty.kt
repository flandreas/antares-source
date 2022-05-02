package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.model.text.Translatable
import ch.scorpion.jabbah.edit.model.text.TranslatablePanel
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.Component
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.JTextComponent

class TranslatablePropertyRenderer(
	multiline: (Translatable) -> Boolean = { _ -> false }
) : DefaultTableCellRenderer() {

	companion object {
		private val BORDER = EmptyBorder(2, 2, 2, 2)
	}

	private val textComponent: JTextComponent?

	init {
		if (multiline.invoke(TranslatableText())) {
			textComponent = JTextArea()
			textComponent.rows = 4
			textComponent.lineWrap = true
			textComponent.wrapStyleWord = true
			textComponent.isEnabled = false
			textComponent.border = BORDER
			textComponent.isOpaque = true
		} else {
			textComponent = null
		}
	}

	override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		val text = if (value is String) {
			value
		} else if (value is Translatable) {
			value.getOptionalTranslation()
		} else {
			""
		}

		return if (textComponent != null) {
			textComponent.text = text
			if (isSelected) {
				textComponent.background = table.selectionBackground
			} else {
				textComponent.background = table.background
			}
			textComponent
		} else {
			super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column)
		}
	}
}

class TranslatablePropertyEditor(
	private val propertyName: String,
	private val multiline: (Translatable) -> Boolean = { _ -> false },
	rows: Int = 4,
	private val editable: Boolean = true
) : AbstractPropertyEditor() {

	private val textComponent: JTextComponent
	private var text: Translatable = TranslatableText()

	init {
		if (multiline.invoke(TranslatableText())) {
			textComponent = JTextArea()
			textComponent.rows = rows
			textComponent.lineWrap = true
			textComponent.wrapStyleWord = true
		} else {
			textComponent = JTextField()
		}
		textComponent.isEditable = editable

		buildUI()
	}

	/** ---- [AbstractPropertyEditor] */

	override fun getValue(): Any {
		return if (StringUtils.isEmpty(textComponent.text)) {
			text.withoutTranslation()
		} else {
			text.withTranslation(textComponent.text)
		}
	}

	override fun setValue(value: Any?) {
		text = value as Translatable? ?: TranslatableText()
		textComponent.text = text.getOptionalTranslation()
	}

	private fun buildUI() {
		val panel = JPanel()

		panel.layout = EGBL.getLayout()
		panel.background = UIManager.getColor("Table.background")

		val button = createButton()

		if (textComponent is JTextArea) {
			val scroll = UiUtil.decorateTextArea(textComponent)
			textComponent.columns = 20
			textComponent.border = UIManager.getBorder("TextField.border")

			EGBL.add(
				panel,
				scroll,
				0, 0,
				1, 1,
				1.0, 1.0,
				EGBL.NORTHWEST,
				EGBL.BOTH,
				0, 0, 0, 0
			)

			EGBL.add(
				panel,
				button,
				1, 0,
				EGBL.REMAINDER, 1,
				0.0, 0.0,
				EGBL.NORTHWEST,
				EGBL.NONE,
				0, 0, 0, 0
			)
		} else {

			EGBL.add(
				panel,
				textComponent,
				0, 0,
				1, 1,
				1.0, 1.0,
				EGBL.WEST,
				EGBL.BOTH,
				0, 0, 0, 0
			)

			EGBL.add(
				panel,
				button,
				1, 0,
				EGBL.REMAINDER, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				0, 0, 0, 0
			)
		}

		editor = panel
	}

	private fun createButton(): JButton {
		val button = JButton()
		button.icon = UiUtil.themedIcon("/img/translation-16.png")
		button.border = BorderFactory.createEmptyBorder(0, 2, 0, 2)
		button.toolTipText = Translations.getString("edit.action.translateText.tooltip")
		button.isOpaque = false
		button.isContentAreaFilled = false // NOTE: This also disables hover highlighting
		button.addActionListener { showDialog() }
		return button
	}

	private fun showDialog() {
		val newText = TranslatablePanel.showAsDialog(
			parent = SwingUtilities.getWindowAncestor(textComponent),
			title = propertyName,
			text = possiblyEditedText,
			textFieldRows = if (multiline.invoke(text)) 8 else 1,
			textFieldColumns = 40,
			editable = editable
		)
		newText?.let { value = it }
	}

	private val possiblyEditedText: Translatable get() = if(StringUtils.isEmpty(textComponent.text)) {
		text.withoutTranslation()
	} else {
		text.withTranslation(textComponent.text)
	}
}