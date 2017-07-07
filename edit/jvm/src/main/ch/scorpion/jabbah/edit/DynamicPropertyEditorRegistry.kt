package ch.scorpion.jabbah.edit

import com.l2fprod.common.propertysheet.Property
import com.l2fprod.common.propertysheet.PropertyEditorRegistry
import java.beans.PropertyEditor

/**
 * Extension of [PropertyEditorRegistry] that allows to register suppliers of [PropertyEditor]s in order to
 * dynamically customize the editors at runtime, for example by filling menus with dynamic content.
 */
class DynamicPropertyEditorRegistry : PropertyEditorRegistry() {

    private val factoryMap: MutableMap<Class<out Any>, (PropertyImpl<*>) -> PropertyEditor> = mutableMapOf()

    @Synchronized override fun getEditor(property: Property): PropertyEditor {
        val registeredSupplier = factoryMap[property.type]
        if (registeredSupplier != null) {
            return registeredSupplier.invoke(property as PropertyImpl<*>)
        }

        return super.getEditor(property)
    }

    fun register(clazz: Class<out Any>, supplier: (PropertyImpl<*>) -> PropertyEditor) {
        factoryMap.put(clazz, supplier)
    }
}