package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class ResistorViewBeanInfo : AnalogComponentViewBeanInfo<ResistorView>() {

	companion object {
		private val resistance = AnalogProperties.resistance()
		private val variable = CommandPropertySwing("variable", "element.property.variable", Boolean::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: ResistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(resistance.bind(editor, beanIdProvider(bean.id)))
		properties.add(variable.bind(editor, beanIdProvider(bean.id)))
	}
}