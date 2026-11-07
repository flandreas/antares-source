package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.view.LightColorRenderer
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.logger
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

class LightColorExpressionEditor(
    propertyName: String,
    editable: Boolean,
    graphEditor: Editor?,
    supportExpressions: Boolean,
    errorCallback: (DslError) -> Unit,
    filter: (LightColor) -> Boolean = { _ -> true }
) : ExpressionPropertyEditor<LightColor>(propertyName, editable, supportExpressions && (graphEditor?.drawing is GraphView), errorCallback) {

    companion object {
        private val LOG by logger(LightColorExpressionEditor::class)
        private val RENDERER = LightColorRenderer()
    }

    private val comboBoxEditor = ComboBoxPropertyEditor()
    @Suppress("UNCHECKED_CAST")

    private val comboBox: JComboBox<LightColor> get() = comboBoxEditor.customEditor as JComboBox<LightColor>

    private val graph = (graphEditor?.drawing as? GraphView?)?.graph
    private val parserFactory = if (graph != null) graph::createParser else null

    init {
        comboBox.renderer = RENDERER
        comboBox.isEnabled = editable
        comboBox.isEditable = editable

        comboBoxEditor.setAvailableValues(LightColor.PREDEFINED.filter { filter(it) }.toTypedArray())

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

    override fun getValueImpl(): LightColor =
        when (val value = comboBox.editor.item) {
            is String -> {
                LOG.trace("The user has entered a script expression")
                parseExpression(value)
            }
            is LightColorExpression -> {
                LOG.trace("The user has entered an expression using the dialog")
                parseExpression("${GraphParamType.EXPRESSION_OP}${value.expression}")
            }
            is LightColor -> {
                LOG.trace("Returning directly LightColor")
                value
            }
            else -> throw IllegalArgumentException("Illegal light color value")
        }

    private fun parseExpression(script: String): LightColor {
        val lightColor = LightColorGraphParamType.parse(script, supportExpressions)
        return if (lightColor is LightColorExpression) {
            graph?.let { lightColor.evaluateIn(it) } ?: lightColor
        } else {
            lightColor
        }
    }

    override fun setValue(value: Any?) {
        comboBox.editor.item = value
    }

    override fun showDialog() {
        val script = when (comboBox.editor.item) {
            is LightColorExpression -> (comboBox.editor.item as LightColorExpression).expression
            is LightColor -> (comboBox.editor.item as LightColor).ordinal.toString()
            else -> (comboBox.editor.item).toString()
        }
        ScriptPropertyPanel.showAsDialog(
            script = script,
            editable = editable,
            propertyName = propertyName,
            variables = graph?.symbolTable?.names(),
            parserFactory = parserFactory
        )?.let {
            // The script is evaluated when the editor loses focus
            comboBox.editor.item = LightColorExpression(it)
        }
    }

}