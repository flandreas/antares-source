package io.antarescircuit.antares.view.inout

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.graph.view.GraphProperties
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