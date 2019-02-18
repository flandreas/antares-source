package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/**
 * A [BeanInfo] for [EdgeViewImpl].
 */
open class EdgeViewImplBeanInfo : ComponentBeanInfo<EdgeViewImpl<*>>() {

	companion object {
		private val arrow = PropertyImpl("graph.property.edgeView.arrow", Boolean::class.java)
		private val layout = PropertyImpl("graph.property.edgeView.layout", LayoutType::class.java)
		private val style = PropertyImpl("graph.property.edgeViewLineStyle", NetViewStyle::class.java)
	}

	override fun addProperties(bean: EdgeViewImpl<*>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		arrow.bind(editor, { bean.isArrow }, { bean.isArrow = it!! })
		layout.bind(editor, { bean.layout.type }, { bean.layout.type = it!! })
		style.bind(editor, { bean.netView!!.style }, { bean.netView!!.style = it!! })

		properties.add(arrow)
		properties.add(layout)
		properties.add(style)
	}
}