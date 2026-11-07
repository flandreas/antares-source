package io.antarescircuit.antares.view.analog

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties

@Suppress("unused") // Reflection
class AnalogLEDViewBeanInfo : AnalogComponentViewBeanInfo<AnalogLEDView>() {

    companion object {
        private val name = EditProperties.untranslatableName()
        private val lightColor = AntaresProperties.lightColor(baseKey = "element.property.LightColor")
        private val minCurrent = AnalogProperties.ampere("minCurrent", "library.element.LightBulb.minCurrent", componentBeanProvider)
        private val maxCurrent = AnalogProperties.ampere("maxCurrent", "library.element.LightBulb.maxCurrent", componentBeanProvider)
    }

    override fun addProperties(bean: AnalogLEDView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        properties.add(name.bind(editor, beanIdProvider(bean.id)))
        properties.add(lightColor.bind(editor, beanIdProvider(bean.id)))
        properties.add(minCurrent.bind(editor, beanIdProvider(bean.id)))
        properties.add(maxCurrent.bind(editor, beanIdProvider(bean.id)))
    }
}