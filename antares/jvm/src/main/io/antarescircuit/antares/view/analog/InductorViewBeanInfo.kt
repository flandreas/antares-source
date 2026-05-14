package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties

@Suppress("unused") // Reflection
class InductorViewBeanInfo : AnalogComponentViewBeanInfo<InductorView>() {

    companion object {
        private val inductance = EditProperties.henry("inductance", "element.property.inductance", componentBeanProvider)
    }

    override fun addProperties(bean: InductorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(inductance.bind(editor, beanIdProvider(bean.id)))
    }
}