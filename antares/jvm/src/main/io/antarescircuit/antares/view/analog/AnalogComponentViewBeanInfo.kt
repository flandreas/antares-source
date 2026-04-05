package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

open class AnalogComponentViewBeanInfo<T : OrientableRectangularVerticeView<*>> : VerticeViewBeanInfo<T>() {

	companion object {
		private val orientation = EditProperties.orientation()
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		if (bean.useOrientation) {
			properties.add(orientation.bind(editor, beanIdProvider(bean.id)))
		}
	}
}