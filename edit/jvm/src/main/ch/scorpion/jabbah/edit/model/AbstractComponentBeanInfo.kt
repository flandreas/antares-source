package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property
import java.beans.SimpleBeanInfo

/**
 * Base class for implementing [SimpleBeanInfo]s for [Component]s.
 * Adds the ID property.
 */
open class AbstractComponentBeanInfo<in T: Component> : AbstractBeanInfo<T>() {

	companion object {
		private val id = EditProperties.id()
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(id.bind(editor, beanIdProvider(bean.id), editable = false))
	}
}