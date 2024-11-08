package ch.scorpion.jabbah.edit.model.image

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.AbstractComponentBeanInfo
import ch.scorpion.jabbah.edit.model.EditProperties
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