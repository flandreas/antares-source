package io.antarescircuit.jabbah.edit.model.image

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class ImageIdentificationBeanInfo : AbstractBeanInfo<ImageIdentification>() {

    companion object {
        private val name = EditProperties.name(beanProvider = applicationDataBeanProvider)
    }

    override fun addProperties(bean: ImageIdentification, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(name.bind(editor, emptyList()))
    }
}