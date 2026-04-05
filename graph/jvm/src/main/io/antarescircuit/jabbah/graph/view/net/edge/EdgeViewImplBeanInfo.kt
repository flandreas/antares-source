package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.AbstractComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphProperties
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyle
import com.l2fprod.common.propertysheet.Property

open class EdgeViewImplBeanInfo : AbstractComponentBeanInfo<EdgeViewImpl<*>>() {

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

		properties.add(modelId.bind(editor, beanIdProvider(bean.id), editable = false))
		properties.add(arrow.bind(editor, beanIdProvider(bean.id)))
		properties.add(layout.bind(editor, beanIdProvider(bean.id), filter = { it.supportsNetViewStyle(bean.netView!!.style)}))
		properties.add(style.bind(editor, beanIdProvider(bean.id), filter = { it.supportsLayoutType(bean.layout.type )}))
		properties.add(color.bind(editor, beanIdProvider(bean.id)))
		properties.add(description.bind(editor, beanIdProvider(bean.id)))
	}
}