package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor

@Suppress("unused") // Reflection
class InductorViewBeanInfo : AnalogComponentViewBeanInfo<InductorView>() {

    companion object {
        private val inductance = AnalogProperties.henry()
        private val variable = AnalogProperties.variable()
    }

    override fun addProperties(bean: InductorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(inductance.bind(editor, beanIdProvider(bean.id)))
        properties.add(variable.bind(editor, beanIdProvider(bean.id)))
    }
}