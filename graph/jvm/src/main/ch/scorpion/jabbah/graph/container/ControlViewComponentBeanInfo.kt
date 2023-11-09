package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.view.ControlViewBeanInfo
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ControlViewComponentBeanInfo : AbstractBeanInfo<ControlViewComponent>() {

	companion object {
		private val LOG by logger(ControlViewComponentBeanInfo::class)
		private val id = EditProperties.id()
		private val modelId = GraphProperties.modelId()
		private val name = EditProperties.untranslatableName()
		private val orientation = EditProperties.orientation()

		const val aggregatePropertyName = "controlView"
	}

	override fun addProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(id.bind(editor, beanIdProvider(bean.id), editable = false))
		properties.add(modelId.bind(editor, beanIdProvider(bean.modelId), editable = false))
		properties.add(name.bind(editor, beanIdProvider(bean.id), editable = false))
		if (bean.useOrientation) {
			properties.add(orientation.bind(editor, beanIdProvider(bean.id)))
		}
		loadControlViewProperties(bean, editor, properties)
	}

	private fun loadControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		val controlViewBeanClass = bean.controlView.javaClass.name + "BeanInfo"
		try {
			val beanInfoClass = Class.forName(controlViewBeanClass)
			val beanInfo = beanInfoClass.getDeclaredConstructor().newInstance()
			if (beanInfo !is ControlViewBeanInfo) {
				return
			}
			beanInfo.addControlViewProperties(bean, editor, properties)
		} catch (e: Throwable) {
			LOG.warn("Could not instantiate Properties for ControlView $controlViewBeanClass: Exception $e")
		}
	}
}