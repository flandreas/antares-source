package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor

@Suppress("unused") // Reflection
class BatteryViewBeanInfo : AnalogComponentViewBeanInfo<BatteryView>() {

	companion object {
		private val voltage = AnalogProperties.volt()
	}

	override fun addProperties(bean: BatteryView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(voltage.bind(editor, beanIdProvider(bean.id)))
	}
}