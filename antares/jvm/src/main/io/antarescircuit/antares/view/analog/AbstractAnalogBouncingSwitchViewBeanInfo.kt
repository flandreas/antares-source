package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

abstract class AbstractAnalogBouncingSwitchViewBeanInfo<T: AbstractAnalogBouncingSwitchView<*>> : AnalogComponentViewBeanInfo<T>() {

    companion object {
        private val closedOnStart = CommandPropertySwing("closedOnStart", SwitchView.CLOSED_ON_START, Boolean::class.java, componentBeanProvider)
        private val bounce = CommandPropertySwing("bounce", SwitchView.BOUNCE, Boolean::class.java, componentBeanProvider)
        private val bounceDuration = CommandPropertySwing("bounceDuration", SwitchView.BOUNCE_DURATION, Long::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(closedOnStart.bind(editor, beanIdProvider(bean.id)))
        properties.add(bounce.bind(editor, beanIdProvider(bean.id)))
        if (bean.model.bounce) {
            properties.add(bounceDuration.bind(editor, beanIdProvider(bean.id)))
        }
    }
}