package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LookupTableViewBeanInfo : DigitalComponentViewBeanInfo<LookupTableView>() {

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