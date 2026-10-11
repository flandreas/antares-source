package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit

@Suppress("unused") // Reflection
class OpAmpViewBeanInfo : AnalogComponentViewBeanInfo<OpAmpView>() {

    companion object {
        private val maxOutput = EditProperties.magnitudeValue(
            "maxOutput", "element.property.OpAmp.maxOutput", componentBeanProvider, SIUnit.Volt)
        private val minOutput = EditProperties.magnitudeValue(
            "minOutput", "element.property.OpAmp.minOutput", componentBeanProvider, SIUnit.Volt)
        val handedness = AntaresProperties.handedness(baseKey = "element.property.OpAmp.handedness")
    }

    override fun addProperties(bean: OpAmpView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
        properties.add(maxOutput.bind(editor, beanIdProvider(bean.id)))
        properties.add(minOutput.bind(editor, beanIdProvider(bean.id)))
    }
}