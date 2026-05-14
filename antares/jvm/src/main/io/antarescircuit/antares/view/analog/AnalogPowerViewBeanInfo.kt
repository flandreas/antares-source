package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.model.EditProperties

@Suppress("unused") // Reflection
class AnalogPowerViewBeanInfo : AnalogComponentViewBeanInfo<AnalogPowerView>() {

	companion object {
		private val voltage = EditProperties.volt("voltage", "element.property.voltage", componentBeanProvider)
	}

	override fun addProperties(bean: AnalogPowerView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(voltage.bind(editor, beanIdProvider(bean.id)))
	}
}