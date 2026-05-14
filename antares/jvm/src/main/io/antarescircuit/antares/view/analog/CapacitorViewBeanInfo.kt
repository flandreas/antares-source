package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties

@Suppress("unused") // Reflection
class CapacitorViewBeanInfo : AnalogComponentViewBeanInfo<CapacitorView>() {

    companion object {
        private val capacitance = EditProperties.farad("capacitance", "element.property.capacitance", componentBeanProvider)
    }

    override fun addProperties(bean: CapacitorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(capacitance.bind(editor, beanIdProvider(bean.id)))
    }
}