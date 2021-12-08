package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class PowerViewBeanInfo : DigitalComponentBeanInfo<PowerView>() {

	override fun addProperties(bean: PowerView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(AntaresProperties.bitWidth(editor = editor).bind(editor, bean.id))
	}
}