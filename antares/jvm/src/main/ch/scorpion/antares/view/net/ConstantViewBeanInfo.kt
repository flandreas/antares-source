package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ConstantViewBeanInfo : DigitalComponentBeanInfo<ConstantView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val signalRep = AntaresProperties.signalRepresentation()
		private val value = CommandPropertySwing("value", "element.property.Constant.value", Long::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: ConstantView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(bitWidth.bind(editor, bean.id))
		properties.add(signalRep.bind(editor, bean.id))
		properties.add(value.bind(editor, bean.id))
	}
}