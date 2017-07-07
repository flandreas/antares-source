package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.graph.model.PortType


/**
 * A [BeanInfo] for [CircuitInOutView].
 */
class CircuitInOutViewBeanInfo : AbstractBeanInfo<CircuitInOutView>() {

    companion object {
        private val id = PropertyImpl("edit.property.id", Int::class.java)
        private val name = PropertyImpl("element.property", String::class.java)
        private val portType = PropertyImpl("graph.property.portType", PortType::class.java)
        private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
        private val orientation = PropertyImpl("graph.property.direction", Direction::class.java)
        private val signalRep = PropertyImpl("element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java)
        private val description = PropertyImpl("edit.property.description", TextProperty::class.java)
    }

    override fun addProperties(bean: CircuitInOutView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        id.bind(editor, { bean.id}, null, false)
		name.bind(editor, { bean.name }, { bean.name = it })
		portType.bind(editor, { bean.portType }, { bean.portType = it!! })
		orientation.bind(editor, { bean.orientation }, { bean.orientation = it!! })
		bitWidth.bind(editor, { bean.bitWidth }, { bean.bitWidth = it!! })
		signalRep.bind(editor, { bean.signalRepresentation }, { bean.signalRepresentation = it!! })
		description.bind(editor, { bean.description}, { bean.description = it!! })

		properties.add(id);
		properties.add(name);
		properties.add(portType);
		properties.add(orientation);
		properties.add(bitWidth);
		properties.add(signalRep);
		properties.add(description);
    }
}