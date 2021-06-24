package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RgbLEDViewBeanInfo : DigitalComponentBeanInfo<RgbLEDView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val square = AntaresProperties.ledSquare()
		private val size = EditProperties.size()
		private val hasBorder = EditProperties.border()
	}

	override fun addProperties(bean: RgbLEDView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, bean.id))
		properties.add(square.bind(editor, bean.id))
		properties.add(size.bind(editor, bean.id))
		properties.add(hasBorder.bind(editor, bean.id))
	}
}