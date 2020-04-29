package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

/**
 * Bean info for [DigitalPortViewComponent].
 */
@Suppress("unused")
class DigitalPortViewComponentBeanInfo : AbstractBeanInfo<DigitalPortViewComponent>() {

    companion object {
	    private val id = PropertyImpl("graph.property.PortId", Int::class.java)
	    private val name = PropertyImpl("element.property", String::class.java)
        private val direction = PropertyImpl("graph.property.direction", Direction::class.java)
        private val portLabelPos = PropertyImpl("graph.property.PortLabelPosition", PortLabelPosition::class.java)
        private val showBitWidth = PropertyImpl("element.property.DigitalPortViewComponent.showBitWidthAnnotation", Boolean::class.java)
        private val logic = PropertyImpl("element.property.logic", Logic::class.java)
        private val trigger = PropertyImpl("element.property.trigger", Trigger::class.java)
        private val outputAnnotation = PropertyImpl("element.property.outputAnnotation", OutputAnnotation::class.java)
    }

    override fun addProperties(bean: DigitalPortViewComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(id.bind(editor, { bean.portId}, null, false))
	    properties.add(name.bind(editor, { bean.port.name }, null, false))
        properties.add(direction.bind(editor, { bean.direction}, { bean.direction = it!! }))
        properties.add(portLabelPos.bind(editor, { bean.portLabelPosition }, { bean.portLabelPosition = it!! }))
        properties.add(showBitWidth.bind(editor, { bean.showBitWidthAnnotation }, { bean.showBitWidthAnnotation = it!! }))
        properties.add(logic.bind(editor, { bean.logic }, { bean.logic = it!! }))
        if (bean.port.portType == PortType.INPUT) {
            properties.add(trigger.bind(editor, { bean.trigger }, { bean.trigger = it!! }))
        }
        if (bean.port.portType == PortType.OUTPUT) {
            properties.add(outputAnnotation.bind(editor,  { bean.outputAnnotation }, { bean.outputAnnotation = it!! }))
        }
    }
}