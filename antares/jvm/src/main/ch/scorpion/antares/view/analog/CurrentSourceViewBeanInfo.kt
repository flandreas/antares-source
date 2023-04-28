package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
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