package io.antarescircuit.jabbah.graph.model.param

import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.ScriptPropertyPanel
import io.antarescircuit.jabbah.graph.view.GraphView
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JComponent
import javax.swing.JTextField

class LongValueEditor(
    propertyName: String,
    editable: Boolean,
    graphEditor: Editor?,
    errorCallback: (DslError) -> Unit
) : ExpressionPropertyEditor<LongValue>(propertyName, editable, true, errorCallback) {

    private val textField = JTextField()

    private val graph = (graphEditor?.drawing as GraphView?)?.graph
    private val parserFactory = if (graph != null) graph::createParser else null

    private var value: LongValue? = null

    init {
        textField.isEnabled = editable

        buildUI()

        if (editable) {
            editor.addFocusListener(object : FocusAdapter() {
                override fun focusGained(e: FocusEvent?) {
                    textField.requestFocusInWindow()
                    textField.selectAll()
                }
            })
        }
    }

    override val contentEditor: JComponent get() = textField

    override fun setValue(value: Any?) {
        if (value is LongValue?) {
            this.value = value
            textField.text = value.toString()
        } else {
            throw IllegalStateException("Illegal number value")
        }
    }

    override fun getValueImpl(): LongValue =
        parseExpression(textField.text)

    private fun parseExpression(script: String): LongValue {
        val longValue = LongValueGraphParamType.parse(script)
        return if (longValue is LongValueExpression) {
            graph?.let { longValue.evaluateIn(it) } ?: longValue
        } else {
            longValue
        }
    }

    override fun showDialog() {
        val script = when (value) {
            is LongValueExpression -> (value as LongValueExpression).expression
            else -> value?.toString() ?: ""
        }
        ScriptPropertyPanel.showAsDialog(
            script = script,
            editable = editable,
            propertyName = propertyName,
            variables = graph?.symbolTable?.names(),
            parserFactory = parserFactory!!
        )?.let {
            value = LongValueExpression(it)
        }
    }
}