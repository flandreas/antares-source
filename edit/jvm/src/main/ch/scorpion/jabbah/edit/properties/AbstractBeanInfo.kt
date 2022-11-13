package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property
import java.beans.SimpleBeanInfo

/**
 * Provides the IDs of the collection of selected beans by featuring [String]
 * to also support concatenated paths of beans.
 */
typealias BeanIdProvider = (Int) -> Collection<String>

/**
 * Base class for implementing [SimpleBeanInfo]s for any kind of beans whose property changes
 * should result in an undoable [Command].
 *
 * The name of concrete [SimpleBeanInfo] classes must adhere to the convention that they
 * extend the bean's class name by "BeanInfo". Example: The [SimpleBeanInfo] class for "LEDView" must be named
 * "LEDViewBeanInfo".
 */
abstract class AbstractBeanInfo<in T>(
	var beanIdProvider: BeanIdProvider = DEFAULT_BEAN_ID_PROVIDER
) : SimpleBeanInfo() {

	companion object {
		private val DEFAULT_BEAN_ID_PROVIDER: BeanIdProvider = { listOf(it.toString()) }
	}

    open fun getProperties(bean: T, editor: Editor): Array<Property> {
        val properties = mutableListOf<Property>()
        addProperties(bean, editor, properties)
        return properties.toTypedArray()
    }

    /**
     * Overridden by subclasses in order to add their custom properties.
     * Public in order to support delegates.
     */
    open fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        // empty
    }
}

/**
 * Base class for implementing [SimpleBeanInfo]s for [Component]s.
 * Adds the ID property.
 */
abstract class ComponentBeanInfo<in T: Component> : AbstractBeanInfo<T>() {

	companion object {
		private val id = EditProperties.id()
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(id.bind(editor, beanIdProvider(bean.id), editable = false))
	}
}