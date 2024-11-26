package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.app.properties.applicationDataBeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class MemoryStorableBeanInfo : AbstractBeanInfo<MemoryStorable>() {

    companion object {
        private val name = EditProperties.name(beanProvider = applicationDataBeanProvider)
        private val addressBitWidth = AntaresProperties.bitWidth("addressWidth",
            "element.property.addressBitWidth", beanProvider = applicationDataBeanProvider, supportExpressions = false)
        private val dataBitWidth = AntaresProperties.bitWidth("dataWidth", "element.property.dataBitWidth",
            beanProvider = applicationDataBeanProvider, supportExpressions = false)
    }

    override fun addProperties(bean: MemoryStorable, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        properties.add(name.bind(editor, emptyList()))
        properties.add(addressBitWidth.bind(editor, emptyList()))
        properties.add(dataBitWidth.bind(editor, emptyList()))
    }
}