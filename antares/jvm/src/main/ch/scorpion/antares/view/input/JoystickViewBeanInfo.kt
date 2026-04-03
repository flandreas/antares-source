package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
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