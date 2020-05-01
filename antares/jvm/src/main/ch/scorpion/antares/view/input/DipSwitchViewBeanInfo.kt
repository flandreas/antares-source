package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class DipSwitchViewBeanInfo : DigitalComponentBeanInfo<DipSwitchView>() {

	companion object {
		private val name = AntaresProperties.untranslatableName()
		private val bitWidth = AntaresProperties.bitWidth()
		private val initialValue = PropertyImpl("initialValue", "element.property.DipSwitch.initialValue", Long::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: DipSwitchView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, bean.id))
		properties.add(bitWidth.bind(editor, bean.id))
		properties.add(initialValue.bind(editor, bean.id))
	}
}