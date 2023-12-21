package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogSwitchViewBeanInfo : AnalogComponentViewBeanInfo<AnalogSwitchView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
	}

	override fun addProperties(bean: AnalogSwitchView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, beanIdProvider(bean.id)))
	}
}