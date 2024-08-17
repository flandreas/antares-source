package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class CapacitorViewBeanInfo : AnalogComponentViewBeanInfo<CapacitorView>() {

    companion object {
        private val capacitance = CommandPropertySwing("capacitance", "element.property.capacitance", Double::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: CapacitorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(capacitance.bind(editor, beanIdProvider(bean.id)))
    }
}