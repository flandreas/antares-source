package ch.scorpion.jabbah.edit.model.image

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.applicationDataBeanProvider
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