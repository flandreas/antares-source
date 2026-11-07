package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.view.inout.AbstractCircuitInOutViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.model.PortType
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class AnalogCircuitInOutViewBeanInfo : AbstractCircuitInOutViewBeanInfo<AnalogCircuitInOutView>() {

    companion object {
        private val description = EditProperties.description()
        private val outputResistance = CommandPropertySwing("outputResistance", "element.property.outputResistance", Long::class.java, componentBeanProvider
        )
    }

    override fun addProperties(bean: AnalogCircuitInOutView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(description.bind(editor, beanIdProvider(bean.id)))
        if (bean.model.portType == PortType.OUTPUT) {
            properties.add(outputResistance.bind(editor, beanIdProvider(bean.id), optional = true))
        }
    }
}