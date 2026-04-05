package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.edit.model.text.TextProperty
import io.antarescircuit.jabbah.edit.model.text.TextPropertyPanel
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.Color
import java.awt.Component
import javax.swing.*
import javax.swing.table.TableCellRenderer

class TextPropertyRenderer : TableCellRenderer {

	private val textArea: JTextArea = JTextArea()

	init {
		textArea.rows = 4
		textArea.lineWrap = true
		textArea.isEditable = true
	}

	override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		if (value is String) {
			textArea.text = value
		} else if (value is TextProperty) {
			textArea.text = value.text
		}
		return textArea
	}
}

/**
 * An editor that provides more space for editing a [TextProperty] by using a multi-row [JTextArea].
 * Additionally contains a button to open a non-modal dialog that provides even more space.
 */
class TextPropertyEditor(private val propertyName: String) : AbstractPropertyEditor() {

	companion object {
		private val LOG by logger(TextPropertyEditor::class)

		// Holds the single [JDialog] instance across all [TextPropertyEditor] instances.
		private var dialog: JDialog? = null

		const val ICON_PATH = "/img/openInPopup-20.png"
	}

	private val editorTextArea: JTextArea = JTextArea()
	private val button = JButton()

	init {
		editorTextArea.rows = 4
		editorTextArea.lineWrap = true
		editorTextArea.isEditable = true

		buildUI()
	}

	override fun getValue(): Any {
		LOG.trace("get value ${editorTextArea.text}")
		return TextProperty(editorTextArea.text)
	}

	override fun setValue(value: Any?) {
		LOG.trace("set value $value")
		editorTextArea.text = (value as TextProperty).text
	}

	private fun buildUI() {
		val panel = JPanel()
		panel.background = Color.WHITE
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		val scrollPane = JScrollPane(editorTextArea)
		scrollPane.alignmentY = Component.TOP_ALIGNMENT
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		panel.add(scrollPane)

		button.isEnabled = dialog == null
		button.alignmentY = Component.TOP_ALIGNMENT
		button.icon = UiUtil.themedIcon(ICON_PATH)
		button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
		button.toolTipText = Translations.getString("edit.action.editText.tooltip")
		button.addActionListener { showDialog() }
		panel.add(button)

		editor = panel
	}

	private fun showDialog() {
		TextPropertyPanel.showAsDialog(title = propertyName, text = editorTextArea.text)?.let {
			editorTextArea.text = it
		}
	}
}