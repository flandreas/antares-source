package ch.scorpion.antares.view.analog

import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogSwitchViewBeanInfo : AnalogComponentViewBeanInfo<AnalogSwitchView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val closedOnStart = CommandPropertySwing("closedOnStart", SwitchView.CLOSED_ON_START, Boolean::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: AnalogSwitchView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(closedOnStart.bind(editor, beanIdProvider(bean.id)))
	}
}