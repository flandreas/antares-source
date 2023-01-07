package ch.scorpion.antares.view.analog

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class LightBulbViewBeanInfo : AnalogComponentViewBeanInfo<LightBulbView>() {

	companion object {
		private val lightColor = AntaresProperties.lightColor()
	}

	override fun addProperties(bean: LightBulbView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
	}

	override var isShowColor: Boolean
		get() = false
		set(value) {
			super.isShowColor = value
		}
}