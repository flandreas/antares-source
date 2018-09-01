package ch.scorpion.antares.view.arithmetic

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** A [BeanInfo] for [RandomView].*/
class RandomViewBeanInfo : DigitalComponentBeanInfo<RandomView>() {

	companion object {
		private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
	}

	override fun addProperties(bean: RandomView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		bitWidth.bind(editor, { bean.bitWidth }) { bean.bitWidth = it!! }

		properties.add(bitWidth)
	}
}