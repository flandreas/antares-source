package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor

@Suppress("unused") // Reflection
class ResistorViewBeanInfo : AnalogComponentViewBeanInfo<ResistorView>() {

	companion object {
		private val resistance = AnalogProperties.resistance()
		private val variable = AnalogProperties.variable()
	}

	override fun addProperties(bean: ResistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(resistance.bind(editor, beanIdProvider(bean.id)))
		properties.add(variable.bind(editor, beanIdProvider(bean.id)))
	}
}