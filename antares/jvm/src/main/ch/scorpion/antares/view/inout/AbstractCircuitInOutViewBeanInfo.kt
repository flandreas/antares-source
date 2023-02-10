package ch.scorpion.antares.view.inout

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.AbstractComponentBeanInfo
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

abstract class AbstractCircuitInOutViewBeanInfo<T: AbstractCircuitInOutView<*>> : AbstractComponentBeanInfo<T>() {

	companion object {
		private val modelId = GraphProperties.modelId()
		private val name = EditProperties.untranslatableName()
		private val orientation = EditProperties.orientation()
		private val color = EditProperties.color()
		private val portType = AntaresProperties.portType()
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(modelId.bind(editor, beanIdProvider(bean.id), editable = false))
		properties.add(name.bind(editor, beanIdProvider(bean.id)))
		properties.add(orientation.bind(editor, beanIdProvider(bean.id)))
		properties.add(color.bind(editor, beanIdProvider(bean.id)))
		properties.add(portType.bind(editor, beanIdProvider(bean.id)))
	}
}