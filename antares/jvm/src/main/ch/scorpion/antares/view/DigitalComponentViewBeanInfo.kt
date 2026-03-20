package ch.scorpion.antares.view

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.graph.view.OrientableRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/**
 * Base class for implementing [BeanInfo]s for [ch.scorpion.jabbah.graph.view.OrientableRectangularVerticeView]s.
 */
open class DigitalComponentViewBeanInfo<T : OrientableRectangularVerticeView<*>> : VerticeViewBeanInfo<T>() {

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