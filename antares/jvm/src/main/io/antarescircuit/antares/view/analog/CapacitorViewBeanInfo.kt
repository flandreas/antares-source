package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor

@Suppress("unused") // Reflection
class CapacitorViewBeanInfo : AnalogComponentViewBeanInfo<CapacitorView>() {

    companion object {
        private val capacitance = AnalogProperties.farad()
        private val variable = AnalogProperties.variable()
    }

    override fun addProperties(bean: CapacitorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(capacitance.bind(editor, beanIdProvider(bean.id)))
        properties.add(variable.bind(editor, beanIdProvider(bean.id)))
    }
}