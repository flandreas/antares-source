package io.antarescircuit.antares.model.fsm

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class FSMDrawingBeanInfo : AbstractBeanInfo<FSMDrawing>() {

    companion object {
        private val name = EditProperties.name(beanProvider = applicationDataBeanProvider)
    }

    override fun addProperties(bean: FSMDrawing, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(name.bind(editor, emptyList()))
    }
}