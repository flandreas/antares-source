package ch.scorpion.antares.view.arithmetic

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class BitExtenderViewBeanInfo : DigitalComponentBeanInfo<BitExtenderView>() {

	companion object {
		private val inputBitWidth = AntaresProperties.bitWidth("inputBitWidth", BitExtenderView.INPUT_BIT_WIDTH_BASE_KEY)
		private val outputBitWidth = AntaresProperties.bitWidth("outputBitWidth", BitExtenderView.OUTPUT_BIT_WIDTH_BASE_KEY)
	}

	override fun addProperties(bean: BitExtenderView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(inputBitWidth.bind(editor, beanIdProvider(bean.id), filter = { it.width < bean.outputBitWidth.width }))
		properties.add(outputBitWidth.bind(editor, beanIdProvider(bean.id), filter = { it.width > bean.inputBitWidth.width }))
	}
}