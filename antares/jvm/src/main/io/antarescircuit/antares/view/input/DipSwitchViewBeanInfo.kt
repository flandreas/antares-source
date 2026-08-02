package io.antarescircuit.antares.view.input

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

@Suppress("unused")
class DipSwitchViewBeanInfo : AbstractAntaresInteractableVerticeBeanInfo<DipSwitchView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val bitWidth = AntaresProperties.bitWidth()
		private val initialValue = CommandPropertySwing("initialValue", "element.property.DipSwitch.initialValue", Long::class.java, componentBeanProvider)
		private val retainValue = CommandPropertySwing("retainValue", "element.property.DipSwitch.retainValue", Boolean::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: DipSwitchView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(initialValue.bind(editor, beanIdProvider(bean.id)))
		properties.add(retainValue.bind(editor, beanIdProvider(bean.id)))
	}
}