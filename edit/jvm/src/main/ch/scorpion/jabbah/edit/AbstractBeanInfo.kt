package ch.scorpion.jabbah.edit

import com.l2fprod.common.propertysheet.Property
import java.beans.SimpleBeanInfo

/**
 * Base class for implementing [SimpleBeanInfo]s for any kind of beans whose property changes
 * should result in an undoable [Command].
 *
 * The name of concrete [SimpleBeanInfo] classes must adhere to the convention that they
 * extend the bean's class name by "BeanInfo". Example: The [SimpleBeanInfo] class for "LEDView" must be named
 * "LEDViewBeanInfo".
 */
abstract class AbstractBeanInfo<T> : SimpleBeanInfo() {

    fun getProperties(bean: T, editor: Editor): Array<Property> {
        val properties = mutableListOf<Property>()
        addProperties(bean, editor, properties)
        return properties.toTypedArray()
    }

    /** Overridden by subclasses in order to add their custom properties.*/
    protected open fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        // empty
    }
}