package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.container.ControlViewComponentBeanInfo
import io.antarescircuit.jabbah.graph.view.ControlViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused") // Reflection
class AnalogLEDControlViewBeanInfo : AbstractBeanInfo<AnalogLEDControlView>(), ControlViewBeanInfo {

    companion object {
        private val shape = AntaresProperties.ledShape(name = "${ControlViewComponentBeanInfo.aggregatePropertyName}.ledShape")
    }

    override fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
        properties.add(shape.bind(editor, beanIdProvider(bean.id)))
    }
}