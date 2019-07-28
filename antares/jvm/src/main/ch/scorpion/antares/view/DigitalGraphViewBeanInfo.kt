package ch.scorpion.antares.view

import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.graph.view.graph.GraphViewImplBeanInfo
import com.l2fprod.common.propertysheet.Property

class DigitalGraphViewBeanInfo : GraphViewImplBeanInfo<DigitalGraphView<*>>() {

	companion object {
		private val defaultLightColor = PropertyImpl("element.property.DigitalGraphView.lightColor", LightColor::class.java)
	}

	override fun addProperties(bean: DigitalGraphView<*>, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		defaultLightColor.bind(editor, { bean.defaultLightColor }, { bean.defaultLightColor = it }, editable = true, optional = true)

		properties.add(defaultLightColor)
	}
}