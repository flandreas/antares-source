package io.antarescircuit.antares.view.input

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

@Suppress("unused")
class DoubleThrowSwitchViewBeanInfo : AbstractAntaresInteractableVerticeBeanInfo<DoubleThrowSwitchView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val toggle = CommandPropertySwing("toggle", SwitchView.BASE_KEY_TOGGLE, Boolean::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: DoubleThrowSwitchView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(toggle.bind(editor, beanIdProvider(bean.id)))
	}
}