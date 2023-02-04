package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
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