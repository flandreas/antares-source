package ch.scorpion.antares.view.arithmetic

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class RandomViewBeanInfo : DigitalComponentBeanInfo<RandomView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
	}

	override fun addProperties(bean: RandomView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, bean.id))
	}
}