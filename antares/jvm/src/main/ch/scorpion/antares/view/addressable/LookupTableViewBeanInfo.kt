package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LookupTableViewBeanInfo : DigitalComponentBeanInfo<LookupTableView>() {

	companion object {
		private val addressBitWidth = AntaresProperties.bitWidth("addressWidth", "element.property.addressBitWidth")
		private val dataBitWidth = AntaresProperties.bitWidth("dataWidth", "element.property.dataBitWidth")
	}

	override fun addProperties(bean: LookupTableView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(addressBitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(dataBitWidth.bind(editor, beanIdProvider(bean.id)))
	}
}