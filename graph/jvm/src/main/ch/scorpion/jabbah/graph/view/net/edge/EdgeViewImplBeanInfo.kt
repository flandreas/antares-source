package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import com.l2fprod.common.propertysheet.Property

open class EdgeViewImplBeanInfo : ComponentBeanInfo<EdgeViewImpl<*>>() {

	companion object {
		private val arrow = PropertyImpl("arrow", "graph.property.edgeView.arrow", Boolean::class.java, componentBeanProvider)
		private val layout = PropertyImpl("layout.type", "graph.property.edgeView.layout", LayoutType::class.java, componentBeanProvider)
		private val style = PropertyImpl("netView.style", "graph.property.edgeViewLineStyle", NetViewStyle::class.java, componentBeanProvider)
		private val description = EditProperties.description()
	}

	override fun addProperties(bean: EdgeViewImpl<*>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(arrow.bind(editor, bean.id))
		properties.add(layout.bind(editor, bean.id))
		properties.add(style.bind(editor, bean.id))
		properties.add(description.bind(editor, bean.id))
	}
}