package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class BreakViewBeanInfo : DigitalComponentViewBeanInfo<BreakView>() {
	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val value = CommandPropertySwing("value", "element.property.Break.value", Long::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: BreakView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(value.bind(editor, beanIdProvider(bean.id)))
	}
}