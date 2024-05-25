package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.ScriptPropertyPanel
import ch.scorpion.jabbah.edit.properties.TextPropertyEditor
import ch.scorpion.jabbah.graph.model.value.LongValue
import ch.scorpion.jabbah.graph.model.value.LongValueExpression
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

// TODO: Basically the same as BitWidthPropertySwing
//  -> can be a common class like GraphParamValueExpressionPropertyBaseSwing
open class LongValuePropertySwing(
    propertyName: String,
    baseKey: String,
    beanProvider: BeanProvider = componentBeanProvider,
    displayName: String? = null
) : CommandPropertySwing<LongValue>(
    propertyName,
    baseKey,
    LongValue::class.java,
    beanProvider,
    interactive = true,
    displayName = displayName
) {
    var dslError: DslError? = null

    override fun isEditable(): Boolean = true

    override fun writeToBeans(force: Boolean) {
        dslError?.let { throw it }
        super.writeToBeans(force)
    }

    override fun readFromObject(bean: Any?) {
        super.readFromObject(bean)
        dslError = null
    }
}

// TODO: Basically the same as BitWidthParamValuePropertySwing
//  -> can be a common class like GraphParamValueExpressionPropertySwing
class LongValueParamValuePropertySwing(
    private val paramDefinition: GraphParamDefinition<LongValue>,
    propertyName: String,
    baseKey: String,
    beanProvider: BeanProvider = componentBeanProvider,
) : LongValuePropertySwing(
    propertyName,
    baseKey,
    beanProvider,
    displayName = "${Translations.getString("$baseKey.name")} '${paramDefinition.name}'"
) {
    override fun readFromObject(bean: Any?) {
        val subGraphVerticeView = bean as SubGraphVerticeViewImpl?
        value = subGraphVerticeView?.model?.paramValues?.getValue(paramDefinition.name)?.value
    }

    override fun createCommand(newValue: LongValue?): AbstractPropertyCommand<LongValue> =
        GraphParamValueCommand(paramDefinition, editor!!, baseKey, emptyArray(), beanProvider, beanIds, newValue)
}

// TODO: Reuse potential with BitWidthEditor
class LongValueEditor(
    private val propertyName: String,
    private val editable: Boolean,
    graphEditor: Editor?,
    private val errorCallback: (DslError) -> Unit,
    filter: (LongValue) -> Boolean = { _ -> true }
) : AbstractPropertyEditor() {

    companion object {
        private val LOG by logger(LongValueEditor::class)
    }

    private val textField = JTextField()
    private val button = JButton()

    private val graph = (graphEditor?.drawing as GraphView?)?.graph
    private val parserFactory = if (graph != null) graph::createParser else null

    private var value: LongValue? = null

    init {
        textField.isEnabled = editable
        button.isEnabled = editable

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

    override fun setValue(value: Any?) {
        if (value is LongValue?) {
            this.value = value
        }
    }

    override fun getValue(): Any? {
        return try {
            getValueImpl()
        } catch (e: DslError) {
            LOG.debug("Parsing value throws $e")
            errorCallback(e)
            null
        } catch (e: Throwable) {
            LOG.debug("Parsing value throws $e")
            errorCallback(DslError(TextLocation.UNDEFINED, e.message ?: "Invalid number expression"))
            null
        }
    }

    private fun getValueImpl(): LongValue {
        return when (value) {
            is LongValueExpression -> {
                parseExpression((value as LongValueExpression).expression)
            }
            is LongValue -> value!!
            else -> throw IllegalStateException("Illegal number value")
        }
    }

    private fun parseExpression(script: String): LongValue {
        val longValue = LongValueGraphParamType.parse(script)
        return if (longValue is LongValueExpression) {
            graph?.let { longValue.evaluateIn(it) } ?: longValue
        } else {
            longValue
        }
    }

    private fun buildUI() {
        val panel = JPanel()
        panel.background = UIManager.getColor("Table.background")
        panel.layout = BorderLayout()

        panel.add(textField, BorderLayout.CENTER)

        button.alignmentY = Component.TOP_ALIGNMENT
        button.icon = UiUtil.themedIcon(TextPropertyEditor.ICON_PATH)
        button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        button.toolTipText = Translations.getString("edit.action.editScript.tooltip")
        button.addActionListener { showDialog() }

        panel.add(button, BorderLayout.EAST)

        editor = panel
    }

    private fun showDialog() {
        val script = when (value) {
            is LongValueExpression -> (value as LongValueExpression).expression
            else -> value?.toString() ?: ""
        }
        ScriptPropertyPanel.showAsDialog(
            script = script,
            editable = editable,
            propertyName = propertyName,
            parserFactory = parserFactory!!
        )?.let {
            value = LongValueExpression(it)
        }
    }
}