package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.CodeLocation
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.ToStringRenderer
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.ScriptPropertyPanel
import ch.scorpion.jabbah.edit.properties.TextPropertyEditor
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamValueCommand
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

open class BitWidthPropertySwing(
	propertyName: String,
	baseKey: String,
	beanProvider: BeanProvider = componentBeanProvider,
	displayName: String? = null
) : CommandPropertySwing<BitWidth>(
	propertyName,
	baseKey,
	BitWidth::class.java,
	beanProvider,
	interactive = true,
	displayName = displayName
) {
	var dslError: DslError? = null

	override fun isEditable(): Boolean = true

	override fun writeToBean(force: Boolean) {
		dslError?.let { throw it }
		super.writeToBean(force)
	}

	override fun readFromObject(bean: Any?) {
		super.readFromObject(bean)
		dslError = null
	}
}

class BitWidthParamValuePropertySwing(
	private val paramDefinition: GraphParamDefinition<BitWidth>,
	propertyName: String,
	baseKey: String,
	beanProvider: BeanProvider = componentBeanProvider,
) : BitWidthPropertySwing(
	propertyName,
	baseKey,
	beanProvider,
	displayName = "${Translations.getString("$baseKey.name")} '${paramDefinition.name}'"
) {

	override fun readFromObject(bean: Any?) {
		val subGraphVerticeView = bean as SubGraphVerticeViewImpl?
		value = subGraphVerticeView?.model?.paramValues?.getValue(paramDefinition.name)?.value
	}

	override fun createCommand(newValue: BitWidth?): AbstractPropertyCommand<BitWidth> =
		GraphParamValueCommand(paramDefinition, editor!!, baseKey, beanProvider, beanIds, newValue)
}

class BitWidthEditor(
	private val propertyName: String,
	private val editable: Boolean,
	graphEditor: Editor?,
	private val errorCallback: (DslError) -> Unit,
	filter: (BitWidth) -> Boolean = { _ -> true }
) : AbstractPropertyEditor() {

	private val comboBoxEditor = ComboBoxPropertyEditor()
	private val button = JButton()
	private val comboBox: JComboBox<BitWidth> get() = comboBoxEditor.customEditor as JComboBox<BitWidth>

	private val graph = (graphEditor?.drawing as GraphView?)?.graph
	private val parserFactory = if (graph != null) graph::createParser else null

	companion object {
		private val LOG by logger(BitWidthEditor::class)
	}

	init {
		comboBox.renderer = ToStringRenderer<BitWidth>()
		comboBox.isEnabled = editable
		comboBox.isEditable = editable
		button.isEnabled = editable

		comboBoxEditor.setAvailableValues(BitWidth.COMMON.filter { filter(it) }.toTypedArray())

		buildUI()

		if (editable) {
			editor.addFocusListener(object : FocusAdapter() {
				override fun focusGained(e: FocusEvent?) {
					comboBox.requestFocusInWindow()
					comboBox.editor.selectAll()
				}
			})
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
			errorCallback(DslError(CodeLocation.UNDEFINED, e.message ?: "Invalid bit width expression"))
			null
		}
	}

	private fun getValueImpl(): BitWidth =
		when (val value = comboBox.editor.item) {
			is String -> {
				LOG.trace("The user has entered a script expression")
				parseExpression(value)
			}
			is BitWidthExpression -> {
				LOG.trace("The user has entered an expression using the dialog")
				parseExpression("=${value.expression}")
			}
			is BitWidth -> {
				LOG.trace("Returning directly BitWidth")
				value
			}
			else -> throw IllegalStateException("Illegal bit width value")
		}

	private fun parseExpression(script: String): BitWidth {
		val bitWidth = BitWidthGraphParamType.parse(script)
		return if (bitWidth is BitWidthExpression) {
			graph?.let { bitWidth.evaluateIn(it) } ?: bitWidth
		} else {
			bitWidth
		}
	}

	override fun setValue(value: Any?) {
		comboBox.editor.item = value
	}

	private fun buildUI() {
		val panel = JPanel()
		panel.background = UIManager.getColor("Table.background")
		panel.layout = BorderLayout()

		panel.add(comboBox, BorderLayout.CENTER)

		button.alignmentY = Component.TOP_ALIGNMENT
		button.icon = UiUtil.themedIcon(TextPropertyEditor.ICON_PATH)
		button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
		button.toolTipText = Translations.getString("edit.action.editScript.tooltip")
		button.addActionListener { showDialog() }

		panel.add(button, BorderLayout.EAST)

		editor = panel
	}

	private fun showDialog() {
		val script = when (comboBox.editor.item) {
			is BitWidthExpression -> (comboBox.editor.item as BitWidthExpression).expression
			else -> (comboBox.editor.item as BitWidth).width.toString()
		}
		ScriptPropertyPanel.showAsDialog(
			script = script,
			editable = editable,
			propertyName = propertyName,
			parserFactory = parserFactory!!
		) ?.let {
			// The script is evaluated when the editor losses focus
			comboBox.editor.item = BitWidthExpression(it)
		}
	}
}