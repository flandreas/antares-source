package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.ComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.graph.model.PortType
import com.l2fprod.common.propertysheet.Property


/**
 * A [AbstractBeanInfo] for [CircuitInOutView].
 */
@Suppress("unused")
class CircuitInOutViewBeanInfo : ComponentBeanInfo<CircuitInOutView>() {

    companion object {
        private val name = PropertyImpl("element.property", String::class.java)
        private val portType = PropertyImpl("graph.property.portType", PortType::class.java)
        private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
        private val orientation = PropertyImpl("graph.property.direction", Direction::class.java)
	    private val color = PropertyImpl("edit.property.color", PredefinedColor::class.java)
        private val signalRep = PropertyImpl("element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java)
	    private val toggle = PropertyImpl("element.property.Switch.toggle", Boolean::class.java)
        private val description = PropertyImpl("edit.property.description", TextProperty::class.java)
    }

    override fun addProperties(bean: CircuitInOutView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

		name.bind(editor, { bean.name }, { bean.name = it })
		portType.bind(editor, { bean.portType }, { bean.portType = it!! })
		orientation.bind(editor, { bean.orientation }, { bean.orientation = it!! })
	    color.bind(editor, { bean.customColor }, { bean.customColor = it })
	    bitWidth.bind(editor, { bean.bitWidth }, { bean.bitWidth = it!! })
		signalRep.bind(editor, { bean.signalRepresentation }, { bean.signalRepresentation = it!! })
	    toggle.bind(editor, { bean.toggle }, { bean.toggle = it!! })
		description.bind(editor, { bean.description}, { bean.description = it!! })

		properties.add(name)
		properties.add(portType)
		properties.add(orientation)
	    properties.add(color)
		properties.add(bitWidth)
		properties.add(signalRep)
	    properties.add(toggle)
		properties.add(description)
    }
}