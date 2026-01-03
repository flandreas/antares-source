package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RgbLEDViewBeanInfo : DigitalComponentViewBeanInfo<RgbLEDView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val shape = AntaresProperties.ledShape()
		private val size = EditProperties.size()
		private val hasBorder = EditProperties.border()
	}

	override fun addProperties(bean: RgbLEDView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(shape.bind(editor, beanIdProvider(bean.id)))
		properties.add(size.bind(editor, beanIdProvider(bean.id)))
		properties.add(hasBorder.bind(editor, beanIdProvider(bean.id)))
	}
}