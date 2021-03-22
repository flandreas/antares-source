package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** Base class for implementing [BeanInfo]s for subclasses of [AbstractVerticeView]s*/
open class VerticeViewBeanInfo<T : AbstractVerticeView<*>> : ComponentBeanInfo<T>() {

	companion object {
		private val propDelay = GraphProperties.propagationDelay(componentBeanProvider)
		private val color = EditProperties.color()
		private val description = EditProperties.description()
		private val shadow = EditProperties.shadow()
	}

	protected open val isShowPropagationDelay: Boolean = true
	protected open var isShowColor: Boolean = true

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		if (isShowPropagationDelay) {
			properties.add(propDelay.bind(editor, bean.id))
		}
		properties.add(shadow.bind(editor, bean.id))
		if (isShowColor) {
			properties.add(color.bind(editor, bean.id))
		}
		properties.add(description.bind(editor, bean.id))
	}
}