package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.TextPropertyEditor
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
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

	override fun getShortDescription(): String? {
		return paramDefinition.description.value
	}
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