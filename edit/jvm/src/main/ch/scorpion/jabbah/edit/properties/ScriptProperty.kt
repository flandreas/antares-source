package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.TextPropertyPanel
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class ScriptPropertyRenderer : DefaultTableCellRenderer() {

	companion object {
		val LABEL_TEXT = Translations.getString("edit.property.script.name")

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

/**
 * An editor that provides more space for editing a [ScriptProperty] by using a multi-row [JTextArea].
 * Additionally contains a button to open a non-modal dialog that provides even more space.
 */
class ScriptPropertyEditor(private val propertyName: String, private val editable: Boolean) : AbstractPropertyEditor() {

	companion object {
		private val LOG by logger(ScriptPropertyEditor::class)

		// Holds the single [JDialog] instance across all [ScriptPropertyEditor] instances.
		private var dialog: JDialog? = null

		private val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
	}

	private val label = JLabel()
	private val button = JButton()
	private var script: ScriptProperty = ScriptProperty()

	init {
		buildUI()
	}

	override fun getValue(): Any {
		LOG.debug("get value $script")
		return script
	}

	override fun setValue(value: Any?) {
		LOG.debug("set value $value")
		script = value as ScriptProperty
		label.text = ScriptPropertyRenderer.getText(script)
	}

	private fun buildUI() {
		val panel = JPanel()
		panel.background = Color.WHITE
		panel.layout = BorderLayout()

		panel.add(label, BorderLayout.CENTER)

		button.isEnabled = dialog == null
		button.alignmentY = Component.TOP_ALIGNMENT
		button.icon = ImageIcon(ScriptPropertyEditor::class.java.getResource(TextPropertyEditor.ICON_PATH))
		button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
		button.toolTipText = Translations.getString("edit.action.editScript.tooltip")
		button.addActionListener { showDialog() }

		panel.add(button, BorderLayout.EAST)

		editor = panel
	}

	private fun showDialog() {
		TextPropertyPanel.showAsDialog(title = propertyName, text = script.scriptOrEmpty, font = FONT, editable = editable)?.let {
			script = ScriptProperty(it)
		}
	}
}