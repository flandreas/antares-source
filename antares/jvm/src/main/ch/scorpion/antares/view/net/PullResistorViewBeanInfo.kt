package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class PullResistorViewBeanInfo : DigitalComponentBeanInfo<PullResistorView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val pullDirection = AntaresProperties.pullDirection()
	}

	override fun addProperties(bean: PullResistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(bitWidth.bind(editor, bean.id))
		properties.add(pullDirection.bind(editor, bean.id))
	}
}