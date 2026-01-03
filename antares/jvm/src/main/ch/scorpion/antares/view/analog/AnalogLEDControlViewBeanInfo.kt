package ch.scorpion.antares.view.analog

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.ControlViewComponentBeanInfo
import ch.scorpion.jabbah.graph.view.ControlViewBeanInfo
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