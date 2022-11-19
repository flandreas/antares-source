package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LEDViewBeanInfo : DigitalComponentViewBeanInfo<LEDView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val lightColor = AntaresProperties.lightColor()
		private val square = AntaresProperties.ledSquare()
		private val size = EditProperties.size()
		private val hasBorder = EditProperties.border()
	}

	override fun addProperties(bean: LEDView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
		properties.add(square.bind(editor, beanIdProvider(bean.id)))
		properties.add(size.bind(editor,beanIdProvider(bean.id)))
		properties.add(hasBorder.bind(editor, beanIdProvider(bean.id)))
	}

	override var isShowColor: Boolean
		get() = false
		set(value) {
			super.isShowColor = value
		}
}