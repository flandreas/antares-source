package io.antarescircuit.antares.view.container

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.OutputAnnotation
import io.antarescircuit.antares.model.Trigger
import io.antarescircuit.antares.view.port.DigitalPortViewStyle
import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.graph.container.InternalLabelOrientation
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition

@Suppress("unused")
class DigitalPortViewComponentBeanInfo : AbstractBeanInfo<DigitalPortViewComponent>() {

    companion object {
	    private val id = CommandPropertySwing("portId", "graph.property.PortId", Int::class.java, componentBeanProvider)
	    private val name = EditProperties.untranslatableName("port.name")
	    private val portViewStyle = CommandPropertySwing("portViewStyle", "element.property.DigitalPortViewStyle", DigitalPortViewStyle::class.java, componentBeanProvider)
	    private val direction = CommandPropertySwing("direction", "graph.property.direction", Direction::class.java, componentBeanProvider)
		private val portLabelPos = CommandPropertySwing("portLabelPosition", "graph.property.PortLabelPosition", PortLabelPosition::class.java, componentBeanProvider)
	    private val internalLabelOrientation = CommandPropertySwing("internalLabelOrientation", "graph.property.internalLabelOrientation", InternalLabelOrientation::class.java, componentBeanProvider)
	    private val largeExtLabelDist = CommandPropertySwing("largeExternalPortLabelDistance", "element.property.DigitalPortViewComponent.largeLabelDist", Boolean::class.java, componentBeanProvider)
	    private val showBitWidth = CommandPropertySwing("showBitWidthAnnotation", "element.property.DigitalPortViewComponent.showBitWidthAnnotation", Boolean::class.java, componentBeanProvider)
	    private val logic = CommandPropertySwing("logic", Logic.BASE_KEY, Logic::class.java, componentBeanProvider)
	    private val trigger = CommandPropertySwing("trigger", "element.property.trigger", Trigger::class.java, componentBeanProvider)
	    private val outputAnnotation = CommandPropertySwing("outputAnnotation", "element.property.outputAnnotation", OutputAnnotation::class.java, componentBeanProvider)
    }

    override fun addProperties(bean: DigitalPortViewComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(id.bind(editor, beanIdProvider(bean.id), editable = false))
	    properties.add(name.bind(editor, beanIdProvider(bean.id), editable = false))
	    properties.add(portViewStyle.bind(editor, beanIdProvider(bean.id)))
	    properties.add(direction.bind(editor, beanIdProvider(bean.id)))
	    properties.add(portLabelPos.bind(editor, beanIdProvider(bean.id)))
	    if (bean.portLabelPosition == PortLabelPosition.INTERNAL) {
		    properties.add(internalLabelOrientation.bind(editor, beanIdProvider(bean.id)))
	    }
	    properties.add(largeExtLabelDist.bind(editor, beanIdProvider(bean.id), editable = bean.portLabelPosition == PortLabelPosition.EXTERNAL))
	    properties.add(showBitWidth.bind(editor, beanIdProvider(bean.id)))
	    properties.add(logic.bind(editor, beanIdProvider(bean.id)))
	    if (bean.port.portType == PortType.INPUT) {
		    properties.add(trigger.bind(editor, beanIdProvider(bean.id)))
	    }
	    if (bean.port.portType == PortType.OUTPUT) {
		    properties.add(outputAnnotation.bind(editor, beanIdProvider(bean.id)))
	    }
    }
}