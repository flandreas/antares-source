package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogDoubleThrowSwitchViewBeanInfo : AnalogComponentViewBeanInfo<AnalogDoubleThrowSwitchView>() {

    companion object {
        private val closedOnStart = CommandPropertySwing("closedOnStart", SwitchView.CLOSED_ON_START, Boolean::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: AnalogDoubleThrowSwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(closedOnStart.bind(editor, beanIdProvider(bean.id)))
    }
}