package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.edit.properties.applicationDataBeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
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