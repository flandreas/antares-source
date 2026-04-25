package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.edit.BeanIdProvider
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property
import java.beans.SimpleBeanInfo

/**
 * Base class for implementing [SimpleBeanInfo]s for any kind of beans whose property changes
 * should result in an undoable [Command].
 *
 * The name of concrete [SimpleBeanInfo] classes must adhere to the convention that they
 * extend the bean's class name by "BeanInfo". Example: The [SimpleBeanInfo] class for "LEDView" must be named
 * "LEDViewBeanInfo".
 *
 * @param T the type of bean being edited
 */
abstract class AbstractBeanInfo<in T>(
	var beanIdProvider: BeanIdProvider = DEFAULT_BEAN_ID_PROVIDER
) : SimpleBeanInfo() {

	companion object {
		val DEFAULT_BEAN_ID_PROVIDER: BeanIdProvider = { listOf(it.toString()) }
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