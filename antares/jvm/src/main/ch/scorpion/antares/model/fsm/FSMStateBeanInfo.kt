package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.AbstractComponentBeanInfo
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class FSMStateBeanInfo : AbstractComponentBeanInfo<FSMState>() {

    companion object {
        private val name = EditProperties.name()
        private val stateType = CommandPropertySwing("stateType", "antares.fsm.state.type", FSMStateType::class.java, componentBeanProvider)
        private val output = CommandPropertySwing("output", "antares.fsm.state.output", String::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: FSMState, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(name.bind(editor, beanIdProvider(bean.id)))
        properties.add(stateType.bind(editor, beanIdProvider(bean.id)))
        properties.add(output.bind(editor, beanIdProvider(bean.id)))
    }
}