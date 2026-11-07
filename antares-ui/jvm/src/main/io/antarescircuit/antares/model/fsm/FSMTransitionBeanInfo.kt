package io.antarescircuit.antares.model.fsm

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class FSMTransitionBeanInfo : AbstractComponentBeanInfo<FSMTransition>() {

    companion object {
        private val condition = CommandPropertySwing("condition", "antares.fsm.transition.condition", String::class.java, componentBeanProvider)
        private val output = CommandPropertySwing("output", "antares.fsm.transition.output", String::class.java, componentBeanProvider)
        private val description = EditProperties.description()
    }

    override fun addProperties(bean: FSMTransition, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(condition.bind(editor, beanIdProvider(bean.id)))
        properties.add(output.bind(editor, beanIdProvider(bean.id)))
        properties.add(description.bind(editor, beanIdProvider(bean.id)))
    }
}