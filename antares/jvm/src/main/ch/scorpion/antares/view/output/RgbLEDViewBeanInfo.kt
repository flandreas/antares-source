package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [RgbLEDView].*/
class RgbLEDViewBeanInfo : DigitalComponentBeanInfo<RgbLEDView>() {

	companion object {
		private val name = PropertyImpl("element.property", String::class.java)
	}

	override fun addProperties(bean: RgbLEDView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		name.bind(editor, { bean.name }, { bean.name = it })

		properties.add(name)
	}
}