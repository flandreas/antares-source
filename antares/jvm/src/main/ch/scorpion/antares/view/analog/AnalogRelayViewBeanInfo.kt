package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogRelayViewBeanInfo : AnalogComponentViewBeanInfo<AnalogRelayView>() {

    companion object {
        private val inductance = CommandPropertySwing("inductance", "element.property.inductance", Double::class.java, componentBeanProvider)
        private val onCurrent = CommandPropertySwing("onCurrent", "element.property.relay.onCurrent", Double::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: AnalogRelayView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(inductance.bind(editor, beanIdProvider(bean.id)))
        properties.add(onCurrent.bind(editor, beanIdProvider(bean.id)))
    }
}