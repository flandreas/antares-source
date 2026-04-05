package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property
import com.l2fprod.common.propertysheet.PropertyRendererRegistry
import com.l2fprod.common.swing.renderer.DefaultCellRenderer
import javax.swing.table.TableCellRenderer

/**
 * Extension of [PropertyRendererRegistry] that allows to register suppliers of [TableCellRenderer]s in order to
 * dynamically customize the renderers at runtime, for example by displaying text with multiple lines.
 */
class DynamicPropertyRendererRegistry : PropertyRendererRegistry() {

	private val factoryMap: MutableMap<Class<out Any>, (CommandPropertySwing<*>) -> TableCellRenderer> = mutableMapOf()

	init {
		// For some reason, the method 'registerDefaults()' in the superclass doesn't register a renderer for String,
		// but this is somehow only missing if [PropertyRendererRegistry] is subclassed.
		val renderer = DefaultCellRenderer()
		renderer.setShowOddAndEvenRows(false)
		registerRenderer(String::class.java, renderer)
	}

	@Synchronized override fun getRenderer(property: Property): TableCellRenderer {
		return factoryMap[property.type]?.invoke(property as CommandPropertySwing<*>) ?: super.getRenderer(property)
	}

	override fun getRenderer(type: Class<*>?): TableCellRenderer {
		return super.getRenderer(type)
	}

	fun register(clazz: Class<out Any>, supplier: (CommandPropertySwing<*>) -> TableCellRenderer) {
		factoryMap[clazz] = supplier
	}
}