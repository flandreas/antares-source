package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class JoystickViewBeanInfo : DigitalComponentBeanInfo<JoystickView>() {
	companion object {
		private val deflection = AntaresProperties.joystickDeflection()
	}

	override fun addProperties(bean: JoystickView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(AntaresProperties.bitWidth(editor = editor).bind(editor, bean.id))
		properties.add(deflection.bind(editor, bean.id))
	}
}