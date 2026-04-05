package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class InductorViewBeanInfo : AnalogComponentViewBeanInfo<InductorView>() {

    companion object {
        private val inductance = CommandPropertySwing("inductance", "element.property.inductance", Double::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: InductorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(inductance.bind(editor, beanIdProvider(bean.id)))
    }
}