package ch.scorpion.antares.view

import java.beans.BeanInfo
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

/**
 * Base class for implementing [BeanInfo]s for [DigitalComponentView]s.
 */
open class DigitalComponentBeanInfo<T : DigitalComponentView<*>> : VerticeViewBeanInfo<T>() {

	companion object {
		private val orientation = PropertyImpl("edit.property.Component.orientation", Direction::class.java)
	}

	override fun addProperties(bean: T, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		orientation.bind(editor, { bean.orientation }, { bean.orientation = it!! })
		properties.add(orientation)
	}
}