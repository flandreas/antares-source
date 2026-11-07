package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class PullResistorViewBeanInfo : DigitalComponentViewBeanInfo<PullResistorView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val pullDirection = AntaresProperties.pullDirection()
	}

	override fun addProperties(bean: PullResistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(pullDirection.bind(editor, beanIdProvider(bean.id)))
	}
}