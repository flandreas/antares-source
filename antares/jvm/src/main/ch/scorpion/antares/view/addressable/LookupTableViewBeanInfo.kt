package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LookupTableViewBeanInfo : DigitalComponentViewBeanInfo<LookupTableView>() {

	companion object {
		private val name = EditProperties.untranslatableName()
		private val addressBitWidth = AntaresProperties.bitWidth("addressWidth", "element.property.addressBitWidth")
		private val dataBitWidth = AntaresProperties.bitWidth("dataWidth", "element.property.dataBitWidth")
	}

	override fun addProperties(bean: LookupTableView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(addressBitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(dataBitWidth.bind(editor, beanIdProvider(bean.id)))
	}
}