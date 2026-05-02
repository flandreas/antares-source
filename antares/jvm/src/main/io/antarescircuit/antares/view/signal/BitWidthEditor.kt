package io.antarescircuit.antares.view.signal

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthExpression
import io.antarescircuit.antares.model.signal.BitWidthGraphParamType
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.swing.ToStringRenderer
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.ScriptPropertyPanel
import io.antarescircuit.jabbah.graph.model.param.ExpressionPropertyEditor
import io.antarescircuit.jabbah.graph.model.param.GraphParamType
import io.antarescircuit.jabbah.graph.view.GraphView
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JTextField

class BitWidthEditor(
	propertyName: String,
	editable: Boolean,
	graphEditor: Editor?,
	supportExpressions: Boolean,
	errorCallback: (DslError) -> Unit,
	filter: (BitWidth) -> Boolean = { _ -> true }
) : ExpressionPropertyEditor<BitWidth>(propertyName, editable, supportExpressions, errorCallback) {

	companion object {
		private val LOG by logger(BitWidthEditor::class)
	}

	private val comboBoxEditor = ComboBoxPropertyEditor()

	@Suppress("UNCHECKED_CAST")
	private val comboBox: JComboBox<BitWidth> get() = comboBoxEditor.customEditor as JComboBox<BitWidth>

	private val graph = (graphEditor?.drawing as GraphView?)?.graph
	private val parserFactory = if (graph != null) graph::createParser else null

	init {
		comboBox.renderer = ToStringRenderer<BitWidth>()
		comboBox.isEnabled = editable
		comboBox.isEditable = editable

		comboBoxEditor.setAvailableValues(BitWidth.COMMON.filter { filter(it) }.toTypedArray())

		buildUI()

		if (editable) {
			editor.addFocusListener(object : FocusAdapter() {
				override fun focusGained(e: FocusEvent?) {
					comboBox.requestFocusInWindow()
					comboBox.editor.selectAll()
				}
			})
			(comboBox.editor.editorComponent as JTextField).addActionListener { comboBox.editor.editorComponent.transferFocus() }
		}
	}

	override val contentEditor: JComponent get() = comboBox

	override fun getValueImpl(): BitWidth =
		when (val value = comboBox.editor.item) {
			is String -> {
				LOG.trace("The user has entered a script expression")
				parseExpression(value)
			}
			is BitWidthExpression -> {
				LOG.trace("The user has entered an expression using the dialog")
				parseExpression("${GraphParamType.EXPRESSION_OP}${value.expression}")
			}
			is BitWidth -> {
				LOG.trace("Returning directly BitWidth")
				value
			}
			else -> throw IllegalStateException("Illegal bit width value")
		}

	private fun parseExpression(script: String): BitWidth {
		val bitWidth = BitWidthGraphParamType.parse(script, supportExpressions)
		return if (bitWidth is BitWidthExpression) {
			graph?.let { bitWidth.evaluateIn(it) } ?: bitWidth
		} else {
			bitWidth
		}
	}

	override fun setValue(value: Any?) {
		comboBox.editor.item = value
	}

	override fun showDialog() {
		val script = when (comboBox.editor.item) {
			is BitWidthExpression -> (comboBox.editor.item as BitWidthExpression).expression
			is BitWidth -> (comboBox.editor.item as BitWidth).width.toString()
			else -> (comboBox.editor.item).toString()
		}
		ScriptPropertyPanel.showAsDialog(
			script = script,
			editable = editable,
			propertyName = propertyName,
			variables = graph?.symbolTable?.names(),
			parserFactory = parserFactory!!
		)?.let {
			// The script is evaluated when the editor loses focus
			comboBox.editor.item = BitWidthExpression(it)
		}
	}
}