package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.view.port.DigitalPortViewStyle
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

@Suppress("unused")
class DigitalPortViewComponentBeanInfo : AbstractBeanInfo<DigitalPortViewComponent>() {

    companion object {
	    private val id = CommandPropertySwing("portId", "graph.property.PortId", Int::class.java, componentBeanProvider)
	    private val name = EditProperties.untranslatableName("port.name")
	    private val portViewStyle = CommandPropertySwing("portViewStyle", "element.property.DigitalPortViewStyle", DigitalPortViewStyle::class.java, componentBeanProvider)
	    private val direction = CommandPropertySwing("direction", "graph.property.direction", Direction::class.java, componentBeanProvider)
		private val portLabelPos = CommandPropertySwing("portLabelPosition", "graph.property.PortLabelPosition", PortLabelPosition::class.java, componentBeanProvider)
	    private val largeExtLabelDist = CommandPropertySwing("largeExternalPortLabelDist", "element.property.DigitalPortViewComponent.largeLabelDist", Boolean::class.java, componentBeanProvider)
	    private val showBitWidth = CommandPropertySwing("showBitWidthAnnotation", "element.property.DigitalPortViewComponent.showBitWidthAnnotation", Boolean::class.java, componentBeanProvider)
	    private val logic = CommandPropertySwing("logic", Logic.BASE_KEY, Logic::class.java, componentBeanProvider)
	    private val trigger = CommandPropertySwing("trigger", "element.property.trigger", Trigger::class.java, componentBeanProvider)
	    private val outputAnnotation = CommandPropertySwing("outputAnnotation", "element.property.outputAnnotation", OutputAnnotation::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: DigitalPortViewComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(id.bind(editor, bean.id, editable = false))
	    properties.add(name.bind(editor, bean.id, editable = false))
	    properties.add(portViewStyle.bind(editor, bean.id))
	    properties.add(direction.bind(editor, bean.id))
	    properties.add(portLabelPos.bind(editor, bean.id))
	    properties.add(largeExtLabelDist.bind(editor, bean.id, editable = bean.portLabelPosition == PortLabelPosition.EXTERNAL))
	    properties.add(showBitWidth.bind(editor, bean.id))
	    properties.add(logic.bind(editor, bean.id))
	    if (bean.port.portType == PortType.INPUT) {
		    properties.add(trigger.bind(editor, bean.id))
	    }
	    if (bean.port.portType == PortType.OUTPUT) {
		    properties.add(outputAnnotation.bind(editor, bean.id))
	    }
    }
}