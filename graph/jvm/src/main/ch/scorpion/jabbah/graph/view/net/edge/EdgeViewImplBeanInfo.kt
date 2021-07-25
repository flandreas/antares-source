package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphProperties
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import com.l2fprod.common.propertysheet.Property

open class EdgeViewImplBeanInfo : ComponentBeanInfo<EdgeViewImpl<*>>() {

	companion object {
		private val modelId = GraphProperties.modelId()
		private val arrow = CommandPropertySwing("arrow", EdgeView.BASE_KEY_ARROW, Boolean::class.java, componentBeanProvider)
		private val layout = CommandPropertySwing("layout.type", LayoutType.BASE_KEY, LayoutType::class.java, componentBeanProvider)
		private val style = CommandPropertySwing("netView.style", NetViewStyle.BASE_KEY, NetViewStyle::class.java, componentBeanProvider)
		private val color = EditProperties.color("netView.customColor")
		private val description = EditProperties.description()
	}

	override fun addProperties(bean: EdgeViewImpl<*>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(modelId.bind(editor, bean.id, editable = false))
		properties.add(arrow.bind(editor, bean.id))
		properties.add(layout.bind(editor, bean.id))
		properties.add(style.bind(editor, bean.id))
		properties.add(color.bind(editor, bean.id))
		properties.add(description.bind(editor, bean.id))
	}
}