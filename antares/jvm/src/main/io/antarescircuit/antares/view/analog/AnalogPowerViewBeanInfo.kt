package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogPowerViewBeanInfo : AnalogComponentViewBeanInfo<AnalogPowerView>() {

	companion object {
		private val voltage = CommandPropertySwing("voltage", "element.property.voltage", Double::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: AnalogPowerView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(voltage.bind(editor, beanIdProvider(bean.id)))
	}
}