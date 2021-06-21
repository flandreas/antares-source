package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RealSwitchViewBeanInfo : DigitalComponentBeanInfo<RealSwitchView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val toggle = CommandPropertySwing("toggle", "element.property.Switch.toggle", Boolean::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: RealSwitchView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, bean.id))
		properties.add(toggle.bind(editor, bean.id))
	}
}