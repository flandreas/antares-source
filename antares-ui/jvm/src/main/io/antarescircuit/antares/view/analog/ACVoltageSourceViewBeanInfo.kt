package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor

@Suppress("unused") // Reflection
class ACVoltageSourceViewBeanInfo : AnalogComponentViewBeanInfo<ACVoltageSourceView>() {

    companion object {
        private val maxVoltage = AnalogProperties.maxVoltage()
        private val frequency = AnalogProperties.frequency()
    }

    override fun addProperties(bean: ACVoltageSourceView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(maxVoltage.bind(editor, beanIdProvider(bean.id)))
        properties.add(frequency.bind(editor, beanIdProvider(bean.id)))
    }
}