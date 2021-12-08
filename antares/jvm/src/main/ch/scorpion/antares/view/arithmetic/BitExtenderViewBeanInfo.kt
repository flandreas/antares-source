package ch.scorpion.antares.view.arithmetic

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class BitExtenderViewBeanInfo : DigitalComponentBeanInfo<BitExtenderView>() {

	override fun addProperties(bean: BitExtenderView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(AntaresProperties.bitWidth("inputBitWidth", BitExtenderView.INPUT_BIT_WIDTH_BASE_KEY, editor = editor)
			.bind(editor, bean.id, filter = { it.width < bean.outputBitWidth.width }))
		properties.add(AntaresProperties.bitWidth("outputBitWidth", BitExtenderView.OUTPUT_BIT_WIDTH_BASE_KEY, editor = editor)
			.bind(editor, bean.id, filter = { it.width > bean.inputBitWidth.width }))
	}
}