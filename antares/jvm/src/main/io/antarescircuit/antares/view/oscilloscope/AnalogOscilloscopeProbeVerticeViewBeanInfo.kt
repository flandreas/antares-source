package io.antarescircuit.antares.view.oscilloscope

import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalType
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")  // Reflection
class AnalogOscilloscopeProbeVerticeViewBeanInfo : OscilloscopeProbeVerticeViewBeanInfo() {

    companion object {
        private val signalType = CommandPropertySwing("signalType", AnalogOscilloscopeSignalType.BASE_KEY, AnalogOscilloscopeSignalType::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: OscilloscopeProbeVerticeView<Any>, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(signalType.bind(editor, beanIdProvider(bean.id)))
    }
}