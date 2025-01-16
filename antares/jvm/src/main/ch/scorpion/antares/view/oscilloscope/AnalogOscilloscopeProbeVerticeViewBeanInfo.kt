package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.analog.AnalogOscilloscopeSignalType
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeViewBeanInfo
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