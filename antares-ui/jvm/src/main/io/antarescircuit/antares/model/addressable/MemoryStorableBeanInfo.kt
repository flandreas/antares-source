package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
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