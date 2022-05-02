package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Parser
import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*

/**
 * An editor that provides more space for editing a [ScriptProperty] by using a multi-row [JTextArea].
 * Additionally contains a button to open a non-modal dialog that provides even more space.
 *
 * @param parserFactory creates the [Parser] used in the "check" function, or `null` if "check" is not supported
 */
class ScriptPropertyEditor(
	private val propertyName: String,
	private val editable: Boolean,
	private val parserFactory: ParserFactory? = BaseModule.parserFactory
) : AbstractPropertyEditor() {

	companion object {
		private val LOG by logger(ScriptPropertyEditor::class)
	}

	private val label = JLabel()
	private val button = JButton()
	private var script: ScriptProperty = ScriptProperty()

	init {
		buildUI()
	}

	override fun getValue(): Any {
		LOG.trace("get value $script")
		return script
	}

	override fun setValue(value: Any?) {
		LOG.trace("set value $value")
		script = value as ScriptProperty
		label.text = ScriptPropertyRenderer.getText(script)
	}

	private fun buildUI() {
		val panel = JPanel()
		panel.background = UIManager.getColor("Table.background")
		panel.layout = BorderLayout()

		panel.add(label, BorderLayout.CENTER)

		button.alignmentY = Component.TOP_ALIGNMENT
		button.icon = UiUtil.themedIcon(TextPropertyEditor.ICON_PATH)
		button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
		button.toolTipText = Translations.getString("edit.action.editScript.tooltip")
		button.isContentAreaFilled = false // NOTE: This also disables hover highlighting
		button.addActionListener { showDialog() }

		panel.add(button, BorderLayout.EAST)

		editor = panel
	}

	private fun showDialog() {
		ScriptPropertyPanel.showAsDialog(
			script = script.scriptOrEmpty,
			editable = editable,
			propertyName = propertyName,
			parserFactory = parserFactory
		) ?.let {
			script = ScriptProperty(it)
		}
	}
}