package io.antarescircuit.jabbah.graph.model.param

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.edit.AbstractPropertyCommand
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.edit.properties.TextPropertyEditor
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*

/**
 * A [CommandPropertySwing] that can handle expressions to determine a property's value.
 */
open class ExpressionPropertySwing<T>(
	propertyName: String,
	baseKey: String,
	valueClass: Class<T>,
	beanProvider: BeanProvider = componentBeanProvider,
	interactive: Boolean = true,
	displayName: String? = null,
	baseKeyParams: Array<Any> = emptyArray(),
	val supportExpressions: Boolean = true
) : CommandPropertySwing<T>(
	propertyName,
	baseKey,
	valueClass,
	beanProvider,
	interactive = interactive,
	displayName = displayName,
	baseKeyParams = baseKeyParams
) {
	/**
	 * Set by editors if parsing the entered expression results in a [DslError].
	 */
	var dslError: DslError? = null

	override fun writeToBeans(force: Boolean) {
		dslError?.let { throw it }
		super.writeToBeans(force)
	}

	override fun readFromObject(bean: Any?) {
		super.readFromObject(bean)
		dslError = null
	}
}

/**
 * Used by [GraphParamValuePropertyFactory] for editing generic properties of [SubGraphVerticeViewImpl].
 */
class GraphParamValuePropertySwing<V : Any>(
	private val paramDefinition: GraphParamDefinition<V>,
	propertyName: String,
	valueClass: Class<V>,
	beanProvider: BeanProvider,
	baseKey: String = if (paramDefinition.hasSemantic) "graph.paramDefs.genericSemanticParameter" else "graph.paramDefs.genericParameter",
	interactive: Boolean = false,
	displayName: String? = null,
	baseKeyParams: Array<Any> = if (paramDefinition.hasSemantic) arrayOf(paramDefinition.name, paramDefinition.semantic!!.translatedName) else arrayOf(paramDefinition.name),
): ExpressionPropertySwing<V>(
	propertyName,
	baseKey,
	valueClass,
	beanProvider,
	interactive,
	displayName,
	baseKeyParams = baseKeyParams
) {

	override fun readFromObject(bean: Any?) {
		val subGraphVerticeView = bean as SubGraphVerticeViewImpl?
		value = subGraphVerticeView?.model?.paramValues?.getValue(paramDefinition.name)?.value
	}

	override fun createCommand(newValue: V?): AbstractPropertyCommand<V> =
		GraphParamValueCommand(paramDefinition, editor!!, baseKey, baseKeyParams, beanProvider, beanIds, newValue)

	override fun getShortDescription(): String? =
		paramDefinition.description.value ?: super.getShortDescription()
}

abstract class ExpressionPropertyEditor<T>(
	protected val propertyName: String,
	protected val editable: Boolean,
	protected val supportExpressions: Boolean = true,
	private val errorCallback: (DslError) -> Unit
) : AbstractPropertyEditor() {

	companion object {
		private val LOG by logger(ExpressionPropertyEditor::class)
	}

	protected abstract val contentEditor: JComponent

	protected abstract fun getValueImpl(): T

	protected abstract fun showDialog()

	protected fun buildUI() {
		val panel = JPanel()
		panel.background = UIManager.getColor("Table.background")
		panel.layout = BorderLayout()

		panel.add(contentEditor, BorderLayout.CENTER)

		if (supportExpressions) {
			val button = JButton()
			button.alignmentY = Component.TOP_ALIGNMENT
			button.icon = UiUtil.themedIcon(TextPropertyEditor.ICON_PATH)
			button.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
			button.toolTipText = Translations.getString("edit.action.editScript.tooltip")
			button.isFocusable = false
			button.addActionListener { showDialog() }
			panel.add(button, BorderLayout.EAST)
		}

		editor = panel
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
			errorCallback(DslError(TextLocation.UNDEFINED, e.message ?: "Invalid expression"))
			null
		}
	}
}