package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class CurrentSourceViewBeanInfo : AnalogComponentViewBeanInfo<CurrentSourceView>() {

	companion object {
		private val current = CommandPropertySwing("current", "element.property.current", Double::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: CurrentSourceView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(current.bind(editor, beanIdProvider(bean.id)))
	}
}