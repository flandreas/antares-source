package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.view.gate.TriStateBufferGateViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TransistorViewBeanInfo : DigitalComponentBeanInfo<TransistorView>() {

	companion object {
		private val transistorType = AntaresProperties.transistorType()
		val handedness = AntaresProperties.handedness(baseKey = "element.property.TriStateBuffer.handedness")
	}

	override fun addProperties(bean: TransistorView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(transistorType.bind(editor, bean.id))
		properties.add(TriStateBufferGateViewBeanInfo.handedness.bind(editor, bean.id))
	}
}