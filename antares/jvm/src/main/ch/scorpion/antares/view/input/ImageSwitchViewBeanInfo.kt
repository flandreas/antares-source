package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class ImageSwitchViewBeanInfo : DigitalComponentViewBeanInfo<ImageSwitchView>() {

    companion object {
        private val onImage = CommandPropertySwing("onImageId", "library.element.ImageSwitch.onImage", ImageIdentification::class.java, componentBeanProvider)
        private val offImage = CommandPropertySwing("offImageId", "library.element.ImageSwitch.offImage", ImageIdentification::class.java, componentBeanProvider)
        private val scale = CommandPropertySwing("scale", "library.element.ImageSwitch.scale", Double::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: ImageSwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(onImage.bind(editor, beanIdProvider(bean.id)))
        properties.add(offImage.bind(editor, beanIdProvider(bean.id)))
        properties.add(scale.bind(editor, beanIdProvider(bean.id)))
    }
}