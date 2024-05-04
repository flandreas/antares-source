package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class ImageSwitchViewBeanInfo : DigitalComponentViewBeanInfo<ImageSwitchView>() {

    companion object {
        private val toggle = CommandPropertySwing("toggle", SwitchView.BASE_KEY_TOGGLE, Boolean::class.java, componentBeanProvider)
        private val minOnTime = CommandPropertySwing("minOnTime", SwitchView.MIN_ON_TIME, Long::class.java, componentBeanProvider)
        private val portDirection = CommandPropertySwing("portDirection", "library.element.ImageSwitch.portDirection", Direction::class.java, componentBeanProvider)
        private val onImage = CommandPropertySwing("onImageId", "library.element.ImageSwitch.onImage", ImageIdentification::class.java, componentBeanProvider)
        private val offImage = CommandPropertySwing("offImageId", "library.element.ImageSwitch.offImage", ImageIdentification::class.java, componentBeanProvider)
        private val scale = CommandPropertySwing("scale", "library.element.ImageSwitch.scale", Double::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: ImageSwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(toggle.bind(editor, beanIdProvider(bean.id)))
        properties.add(portDirection.bind(editor, beanIdProvider(bean.id)))
        properties.add(onImage.bind(editor, beanIdProvider(bean.id)))
        properties.add(offImage.bind(editor, beanIdProvider(bean.id)))
        properties.add(scale.bind(editor, beanIdProvider(bean.id)))

		if (!bean.toggle) {
			properties.add(minOnTime.bind(editor, beanIdProvider(bean.id)))
		}
    }
}