package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TransistorViewBeanInfo : DigitalComponentBeanInfo<TransistorView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val transistorType = AntaresProperties.transistorType()
		private val bitWidth = AntaresProperties.bitWidth()
		private val handedness = AntaresProperties.handedness(baseKey = Handedness.BASE_KEY)
	}

	override fun addProperties(bean: TransistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(name.bind(editor, bean.id))
		properties.add(transistorType.bind(editor, bean.id))
		properties.add(bitWidth.bind(editor, bean.id))
		properties.add(handedness.bind(editor, bean.id))
	}
}