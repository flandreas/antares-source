package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LEDViewBeanInfo : DigitalComponentBeanInfo<LEDView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val lightColor = AntaresProperties.lightColor()
	}

	override fun addProperties(bean: LEDView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, bean.id))
		properties.add(lightColor.bind(editor, bean.id))
	}

	override var isShowColor: Boolean
		get() = false
		set(value) {
			super.isShowColor = value
		}
}