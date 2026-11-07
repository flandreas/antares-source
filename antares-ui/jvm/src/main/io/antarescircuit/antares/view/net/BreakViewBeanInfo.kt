package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
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