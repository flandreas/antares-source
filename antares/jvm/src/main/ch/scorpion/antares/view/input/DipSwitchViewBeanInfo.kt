package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class DipSwitchViewBeanInfo : DigitalComponentViewBeanInfo<DipSwitchView>() {

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