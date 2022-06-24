package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class WireTapViewBeanInfo : DigitalComponentBeanInfo<WireTapView>() {

	companion object {
		private val inputBitWidth = AntaresProperties.bitWidth("inputBitWidth", "library.element.WireTap.inputBitWidth")
		private val outputBitWidth = AntaresProperties.bitWidth("outputBitWidth", "library.element.WireTap.outputBitWidth")
		private val portViewSpacing = AntaresProperties.portViewSpacing()
	}

	override fun addProperties(bean: WireTapView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(inputBitWidth.bind(editor, bean.id))
		properties.add(outputBitWidth.bind(editor, bean.id))
		properties.add(portViewSpacing.bind(editor, bean.id))
	}
}