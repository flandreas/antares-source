package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.AbstractComponentBeanInfo
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.ControlViewComponentBeanInfo
import ch.scorpion.jabbah.graph.view.ControlViewBeanInfo
import ch.scorpion.jabbah.graph.view.GraphProperties
import ch.scorpion.jabbah.graph.view.LabeledRectangularVerticeView
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/** Base class for implementing [BeanInfo]s for subclasses of [AbstractVerticeView]s. */
open class VerticeViewBeanInfo<T : AbstractVerticeView<*>> : AbstractComponentBeanInfo<T>(), ControlViewBeanInfo {

	companion object {
		private val modelId = GraphProperties.modelId()
		private val propDelay = GraphProperties.propagationDelay()
		private val color = EditProperties.color()
		private val name = EditProperties.untranslatableName()
		private val showName = GraphProperties.showExternalLabel()
		private val description = EditProperties.description()
		private val shadow = EditProperties.shadow()
		private val controlViewShowName = GraphProperties.showExternalLabel(name = "${ControlViewComponentBeanInfo.aggregatePropertyName}.showExternalLabel")
	}

	protected open val isShowPropagationDelay: Boolean = true
	protected open var isShowColor: Boolean = true
	protected open val isShowName: Boolean = true

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(modelId.bind(editor, beanIdProvider(bean.id), editable = false))
		if (isShowPropagationDelay) {
			properties.add(propDelay.bind(editor, beanIdProvider(bean.id)))
		}
		properties.add(shadow.bind(editor, beanIdProvider(bean.id)))
		if (isShowColor) {
			properties.add(color.bind(editor, beanIdProvider(bean.id)))
		}
		if (isShowName && bean is LabeledRectangularVerticeView<*>) {
			properties.add(name.bind(editor, beanIdProvider(bean.id)))
			properties.add(showName.bind(editor, beanIdProvider(bean.id)))
		}
		properties.add(description.bind(editor, beanIdProvider(bean.id)))
	}

	override fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		if (isShowName && bean.controlView is LabeledRectangularVerticeView<*>)  {
			properties.add(controlViewShowName.bind(editor, beanIdProvider(bean.id)))
		}
	}
}