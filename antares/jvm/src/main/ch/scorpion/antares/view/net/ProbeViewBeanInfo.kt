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
        private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
        private val signalRep = PropertyImpl("element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java)
        private val output = PropertyImpl("element.property.hasOutput", Boolean::class.java)
    }

    override fun addProperties(bean: ProbeView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        bitWidth.bind(editor, { bean.bitWidth }) { bean.bitWidth = it!! }
        signalRep.bind(editor, { bean.signalRepresentation }) { bean.signalRepresentation = it!! }
        output.bind(editor, { bean.hasOutput }) { bean.hasOutput = it!! }

        properties.add(bitWidth)
        properties.add(signalRep)
        properties.add(output)
    }
}