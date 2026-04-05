package io.antarescircuit.jabbah.graph.model.param

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.edit.properties.TextPropertyEditor
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.ui.param.GraphParamDefinitionsViewSwing
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class GraphParamDefinitionsPropertyRenderer : DefaultTableCellRenderer() {

	companion object {
		fun getText(params: GraphParamDefinitions): String =
			if (params.isEmpty) {
				Translations.getString("graph.property.graphParams.noParams")
			} else {
				Translations.getString("graph.property.graphParams.nParams", params.size)
			}
	}

	override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
		label.text = getText(value as GraphParamDefinitions)
		return label
	}
}

/**
 * TODO: Unify with ScriptPropertyEditor.
 */
class GraphParamDefinitionsPropertyEditor(
	private val propertyName: String,
	private val editable: Boolean,
	private val graph: Graph
) : AbstractPropertyEditor() {

	private val label = JLabel()
	private val button = JButton()
	private var paramDefs: GraphParamDefinitions = GraphParamDefinitions()

	init {
		buildUI()
	}

	override fun getValue(): Any = paramDefs

	override fun setValue(value: Any?) {
		paramDefs = value as GraphParamDefinitions
		label.text = GraphParamDefinitionsPropertyRenderer.getText(paramDefs)
	}

	private fun buildUI() {
		val panel = JPanel()
		panel.background = UIManager.getColor("Table.background")
		panel.layout = BorderLayout()
		panel.border = BorderFactory.createLineBorder(UIManager.getColor("Component.focusColor"))

		panel.add(label, BorderLayout.CENTER)

		button.alignmentY = Component.TOP_ALIGNMENT
		// TODO Use distinctive icon
		button.icon = UiUtil.themedIcon(TextPropertyEditor.ICON_PATH)
		button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
		button.toolTipText = Translations.getString("graph.property.graphParams.tooltip")
		button.isContentAreaFilled = false // NOTE: This also disables hover highlighting
		button.addActionListener { showDialog() }

		panel.add(button, BorderLayout.EAST)

		editor = panel
	}

	private fun showDialog() {
		GraphParamDefinitionsViewSwing.showAsDialog(JFrame.getFrames()[0], graph)?.let {
			paramDefs = it
		}
	}
}