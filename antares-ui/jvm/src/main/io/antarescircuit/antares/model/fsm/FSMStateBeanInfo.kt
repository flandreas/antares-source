package io.antarescircuit.antares.model.fsm

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class FSMStateBeanInfo : AbstractComponentBeanInfo<FSMState>() {

    companion object {
        private val stateNumber = CommandPropertySwing("stateNumber", "antares.fsm.state.number", Int::class.java, componentBeanProvider)
        private val stateType = CommandPropertySwing("stateType", "antares.fsm.state.type", FSMStateType::class.java, componentBeanProvider)
        private val name = EditProperties.name()
        private val description = EditProperties.description()
        private val output = CommandPropertySwing("output", "antares.fsm.state.output", String::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: FSMState, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(stateType.bind(editor, beanIdProvider(bean.id)))
        properties.add(stateNumber.bind(editor, beanIdProvider(bean.id)))
        properties.add(name.bind(editor, beanIdProvider(bean.id)))
        properties.add(output.bind(editor, beanIdProvider(bean.id)))
        properties.add(description.bind(editor, beanIdProvider(bean.id)))
    }
}