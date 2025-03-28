package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.applicationDataBeanProvider
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