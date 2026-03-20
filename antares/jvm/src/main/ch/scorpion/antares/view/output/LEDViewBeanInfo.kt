package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.ControlViewComponentBeanInfo
import ch.scorpion.jabbah.graph.view.ControlViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LEDViewBeanInfo : DigitalComponentViewBeanInfo<LEDView>(), ControlViewBeanInfo {

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
		properties.add(controlViewLightColor.bind(editor, beanIdProvider(bean.id)))
	}

	override var isShowColor: Boolean
		get() = false
		set(value) {
			super.isShowColor = value
		}
}