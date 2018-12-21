package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import com.l2fprod.common.propertysheet.PropertyRendererRegistry
import javax.swing.table.TableCellRenderer

/**
 * Extension of [PropertyRendererRegistry] that allows to register suppliers of [TableCellRenderer]s in order to
 * dynamically customize the renderers at runtime, for example by displaying text with multiple lines.
 */
class DynamicPropertyRendererRegistry : PropertyRendererRegistry() {

	private val factoryMap: MutableMap<Class<out Any>, (PropertyImpl<*>) -> TableCellRenderer> = mutableMapOf()

	@Synchronized override fun getRenderer(property: Property): TableCellRenderer {
		return factoryMap[property.type]?.invoke(property as PropertyImpl<*>) ?: super.getRenderer(property)
	}

	fun register(clazz: Class<out Any>, supplier: (PropertyImpl<*>) -> TableCellRenderer) {
		factoryMap[clazz] = supplier
	}
}