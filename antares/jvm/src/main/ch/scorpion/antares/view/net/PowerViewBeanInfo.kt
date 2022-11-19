package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class PowerViewBeanInfo : DigitalComponentViewBeanInfo<PowerView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
	}

	override fun addProperties(bean: PowerView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
	}
}