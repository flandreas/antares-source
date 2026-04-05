package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.input.SwitchConfiguration
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogRelayViewBeanInfo : AnalogComponentViewBeanInfo<AnalogRelayView>() {

    companion object {
        private val switchConfig = CommandPropertySwing("switchConfiguration", "element.property.switchConfiguration", SwitchConfiguration::class.java, componentBeanProvider)
        private val inductance = CommandPropertySwing("inductance", "element.property.inductance", Double::class.java, componentBeanProvider)
        private val onCurrent = CommandPropertySwing("onCurrent", "element.property.relay.onCurrent", Double::class.java, componentBeanProvider)
        private val normallyOn = CommandPropertySwing("normallyOn", "element.property.relay.normallyOn", Boolean::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: AnalogRelayView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(switchConfig.bind(editor, beanIdProvider(bean.id), editable = !bean.model.isConnected))
        properties.add(inductance.bind(editor, beanIdProvider(bean.id)))
        properties.add(onCurrent.bind(editor, beanIdProvider(bean.id)))
        if (bean.switchConfiguration == SwitchConfiguration.SPST) {
            properties.add(normallyOn.bind(editor, beanIdProvider(bean.id)))
        }
    }
}