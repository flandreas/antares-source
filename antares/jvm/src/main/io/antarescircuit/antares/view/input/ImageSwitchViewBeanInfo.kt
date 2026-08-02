package io.antarescircuit.antares.view.input

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

@Suppress("unused") // Reflection
class ImageSwitchViewBeanInfo : AbstractAntaresInteractableVerticeBeanInfo<ImageSwitchView>() {

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