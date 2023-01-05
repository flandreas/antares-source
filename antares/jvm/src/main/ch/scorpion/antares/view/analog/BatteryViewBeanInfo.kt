package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class BatteryViewBeanInfo : AnalogComponentViewBeanInfo<BatteryView>() {

	companion object {
		private val voltage = CommandPropertySwing("voltage", "element.property.voltage", Double::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: BatteryView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(voltage.bind(editor, beanIdProvider(bean.id)))
	}
}