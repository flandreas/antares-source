package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor

@Suppress("unused") // Reflection
class CurrentSourceViewBeanInfo : AnalogComponentViewBeanInfo<CurrentSourceView>() {

	companion object {
		private val current = AnalogProperties.ampere()
	}

	override fun addProperties(bean: CurrentSourceView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(current.bind(editor, beanIdProvider(bean.id)))
	}
}