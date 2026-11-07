package io.antarescircuit.antares.view.arithmetic

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RandomViewBeanInfo : DigitalComponentViewBeanInfo<RandomView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
	}

	override fun addProperties(bean: RandomView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
	}
}