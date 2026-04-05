package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class CapacitorViewBeanInfo : AnalogComponentViewBeanInfo<CapacitorView>() {

    companion object {
        private val capacitance = CommandPropertySwing("capacitance", "element.property.capacitance", Double::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: CapacitorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(capacitance.bind(editor, beanIdProvider(bean.id)))
    }
}