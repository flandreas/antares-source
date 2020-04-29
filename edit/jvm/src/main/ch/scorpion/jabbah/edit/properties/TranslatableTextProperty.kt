package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
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

	private val textComponent: JTextComponent

	init {
		if (multiline.invoke(TranslatableText())) {
			textComponent = JTextArea()
			textComponent.rows = 4
			textComponent.lineWrap = true
			textComponent.wrapStyleWord = true
			textComponent.isEnabled = false
			textComponent.border = null
		} else {
			textComponent = JTextField()
			textComponent.preferredSize = Dimension(70, 22)
			textComponent.border = null
		}
	}

	override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
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

		return textComponent
	}
}

class TranslatableTextPropertyEditor(
	private val propertyName: String,
	private val multiline: (TranslatableText) -> Boolean = { _ -> false },
	rows: Int = 4
) : AbstractPropertyEditor() {

	private val textComponent: JTextComponent
	private var text: TranslatableText = TranslatableText()

	init {
		if (multiline.invoke(text)) {
			textComponent = JTextArea()
			textComponent.rows = rows
			textComponent.lineWrap = true
			textComponent.wrapStyleWord = true
			textComponent.isEditable = true
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

		panel.layout = EGBL.getLayout()

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
		button.icon = ImageIcon(TextPropertyEditor::class.java.getResource("/img/translation-16.png"))
		button.border = BorderFactory.createEmptyBorder(0, 2, 0, 2)
		button.toolTipText = Translations.getString("edit.action.translateText.tooltip")
		button.addActionListener { showDialog() }
		return button
	}

	private fun showDialog() {
		val newText = TranslatableTextPanel.showAsDialog(
			parent = SwingUtilities.getWindowAncestor(textComponent),
			title = propertyName,
			text = text,
			textFieldRows = if (multiline.invoke(text)) 8 else 1,
			textFieldColumns = 40
		)
		newText?.let { value = it }
	}
}