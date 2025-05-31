package ch.scorpion.antares.view.analog

import ch.scorpion.antares.view.inout.AbstractCircuitInOutViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.model.PortType
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