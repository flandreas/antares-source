package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor


/**
 * A [BeanInfo] for [ProbeView].
 */
class ProbeViewBeanInfo : DigitalComponentBeanInfo<ProbeView>() {

    companion object {
	    private val name = PropertyImpl("element.property", String::class.java)
        private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
        private val signalRep = PropertyImpl("element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java)
        private val output = PropertyImpl("element.property.hasOutput", Boolean::class.java)
	    private val logging = PropertyImpl("element.property.logging", Boolean::class.java)
    }

    override fun addProperties(bean: ProbeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    name.bind(editor, { bean.name }, { bean.name = it })
        bitWidth.bind(editor, { bean.bitWidth }, { bean.bitWidth = it!! })
        signalRep.bind(editor, { bean.signalRepresentation }, { bean.signalRepresentation = it!! })
        output.bind(editor, { bean.hasOutput }, { bean.hasOutput = it!! }, !bean.model.isConnected)
	    logging.bind(editor, { bean.isLogging }, { bean.isLogging = it!! })

	    properties.add(name)
        properties.add(bitWidth)
        properties.add(signalRep)
        properties.add(output)
	    properties.add(logging)
    }
}