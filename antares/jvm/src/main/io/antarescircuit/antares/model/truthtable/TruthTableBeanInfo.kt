package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class TruthTableBeanInfo : AbstractBeanInfo<TruthTable>() {

    companion object {
        private val name = EditProperties.name(beanProvider = applicationDataBeanProvider)
        private val description = EditProperties.description(beanProvider = applicationDataBeanProvider)
    }

    override fun addProperties(bean: TruthTable, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(name.bind(editor, emptyList()))
        properties.add(description.bind(editor, emptyList()))
    }
}