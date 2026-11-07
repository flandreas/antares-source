package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class JoystickViewBeanInfo : DigitalComponentViewBeanInfo<JoystickView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val deflection = AntaresProperties.joystickDeflection()
	}

	override fun addProperties(bean: JoystickView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(deflection.bind(editor, beanIdProvider(bean.id)))
	}
}