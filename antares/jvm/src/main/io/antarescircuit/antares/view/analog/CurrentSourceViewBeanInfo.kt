package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties

@Suppress("unused") // Reflection
class CurrentSourceViewBeanInfo : AnalogComponentViewBeanInfo<CurrentSourceView>() {

	companion object {
		private val current = EditProperties.ampere("current", "element.property.current", componentBeanProvider)
	}

	override fun addProperties(bean: CurrentSourceView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(current.bind(editor, beanIdProvider(bean.id)))
	}
}