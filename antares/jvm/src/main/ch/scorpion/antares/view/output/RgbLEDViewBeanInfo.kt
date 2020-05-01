package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RgbLEDViewBeanInfo : DigitalComponentBeanInfo<RgbLEDView>() {

	companion object {
		private val name = AntaresProperties.untranslatableName()
	}

	override fun addProperties(bean: RgbLEDView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, bean.id))
	}
}