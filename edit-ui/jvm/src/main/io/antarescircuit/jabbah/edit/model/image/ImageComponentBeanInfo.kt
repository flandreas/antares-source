package io.antarescircuit.jabbah.edit.model.image

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ImageComponentBeanInfo : AbstractComponentBeanInfo<ImageComponent>() {

    companion object {
        private val name = EditProperties.name()
    }

    override fun addProperties(bean: ImageComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(name.bind(editor, beanIdProvider(bean.id), editable = false))
    }
}