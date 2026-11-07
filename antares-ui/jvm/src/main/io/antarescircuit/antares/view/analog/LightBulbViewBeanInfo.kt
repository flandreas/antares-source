package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class LightBulbViewBeanInfo : AnalogComponentViewBeanInfo<LightBulbView>() {

	companion object {
		private val lightColor = AntaresProperties.lightColor(baseKey = "element.property.LightColor")
		private val resistance = AnalogProperties.resistance()
		private val minCurrent = CommandPropertySwing("minCurrent", "library.element.LightBulb.minCurrent", Double::class.java, componentBeanProvider)
		private val maxCurrent = CommandPropertySwing("maxCurrent", "library.element.LightBulb.maxCurrent", Double::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: LightBulbView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
		properties.add(resistance.bind(editor, beanIdProvider(bean.id)))
		properties.add(minCurrent.bind(editor, beanIdProvider(bean.id)))
		properties.add(maxCurrent.bind(editor, beanIdProvider(bean.id)))
	}
}