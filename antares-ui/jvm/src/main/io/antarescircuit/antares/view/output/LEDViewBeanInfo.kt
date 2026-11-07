package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.container.ControlViewComponentBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LEDViewBeanInfo : DigitalComponentViewBeanInfo<LEDView>() {

	companion object {
		private val lightColor = AntaresProperties.lightColor()
		private val shape = AntaresProperties.ledShape()
		private val size = EditProperties.size()
		private val hasBorder = EditProperties.border()

		private val controlViewLightColor = AntaresProperties.lightColor(name = "${ControlViewComponentBeanInfo.aggregatePropertyName}.lightColor")
	}

	override fun addProperties(bean: LEDView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
		properties.add(shape.bind(editor, beanIdProvider(bean.id)))
		properties.add(size.bind(editor,beanIdProvider(bean.id)))
		properties.add(hasBorder.bind(editor, beanIdProvider(bean.id)))
	}

	override fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		super.addControlViewProperties(bean, editor, properties)
		properties.add(controlViewLightColor.bind(editor, beanIdProvider(bean.id)))
	}

	override var isShowColor: Boolean
		get() = false
		set(value) {
			super.isShowColor = value
		}
}