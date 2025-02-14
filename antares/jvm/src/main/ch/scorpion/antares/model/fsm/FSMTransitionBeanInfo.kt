package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.AbstractComponentBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class FSMTransitionBeanInfo : AbstractComponentBeanInfo<FSMTransition>() {

    companion object {
        private val condition = CommandPropertySwing("condition", "antares.fsm.transition.condition", String::class.java, componentBeanProvider)
        private val output = CommandPropertySwing("output", "antares.fsm.transition.output", String::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: FSMTransition, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(condition.bind(editor, beanIdProvider(bean.id)))
        properties.add(output.bind(editor, beanIdProvider(bean.id)))
    }
}