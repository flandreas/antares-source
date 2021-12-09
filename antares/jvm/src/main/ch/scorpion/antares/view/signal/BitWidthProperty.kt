package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthExpression
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
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
import javax.swing.table.TableCellRenderer

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

class BitWidthRenderer : DefaultListCellRenderer(), TableCellRenderer {

	override fun getListCellRendererComponent(list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
		setValue(value as BitWidth?)

		if (isSelected) {
			foreground = list.selectionForeground
			background = list.selectionBackground
		} else {
			foreground = list.foreground
			background = list.background
		}
		font = list.font
		return this
	}

	override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
		setValue(value as BitWidth?)

		if (isSelected) {
			foreground = table.selectionForeground
			background = table.selectionBackground
		} else {
			foreground = table.foreground
			background = table.background
		}
		font = table.font

		return this
	}

	private fun setValue(bitWidth: BitWidth?) {
		text = bitWidth?.toString() ?: ""
	}
}

class BitWidthEditor(
	private val propertyName: String,
	private val editable: Boolean,
	private val graphEditor: Editor,
	private val errorCallback: (DslError) -> Unit,
	filter: (BitWidth) -> Boolean = { _ -> true }
) : AbstractPropertyEditor() {

	private val comboBoxEditor = ComboBoxPropertyEditor()
	private val button = JButton()
	private val comboBox: JComboBox<BitWidth> get() = comboBoxEditor.customEditor as JComboBox<BitWidth>

	private val graph = (graphEditor.drawing as GraphView).graph!!
	private val parserFactory = graph::createParser

	private val isExpression: Boolean get() = parserFactory != null && comboBox.selectedItem is BitWidthExpression

	init {
		comboBox.renderer = BitWidthRenderer()

		val list = BitWidth.PREDEFINED.filter { filter(it) }.toMutableList()
		if (parserFactory != null) {
			list.add(BitWidthExpression(""))
		}
		comboBoxEditor.setAvailableValues(list.toTypedArray())

		comboBox.addItemListener {
			updateButton()
		}

		buildUI()

		editor.addFocusListener(object : FocusAdapter() {
			override fun focusGained(e: FocusEvent?) {
				comboBox.requestFocusInWindow()
				comboBox.showPopup()
			}
		})
	}

	override fun getValue(): Any = comboBoxEditor.value

	override fun setValue(value: Any?) {
		super.setValue(value)
		comboBoxEditor.value = value
		updateButton()
	}

	private fun updateButton() {
		button.isEnabled = isExpression
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
		ScriptPropertyPanel.showAsDialog(
			script = (comboBox.selectedItem as BitWidthExpression).expression,
			editable = editable,
			propertyName = propertyName,
			parserFactory = parserFactory!!
		) ?.let {
			(comboBox.selectedItem as BitWidthExpression).expression = it

			try {
				(comboBox.selectedItem as BitWidthExpression).evaluateIn(graph)?.let { value ->
					(comboBox.selectedItem as BitWidthExpression).value = value
				}
			} catch (e: DslError) {
				errorCallback(e)
			}
		}
	}
}