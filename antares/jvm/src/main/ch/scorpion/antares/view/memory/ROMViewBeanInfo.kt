package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl


/**
 * A [BeanInfo] for [ROMView].
 */
class ROMViewBeanInfo : DigitalComponentBeanInfo<ROMView>() {

    companion object {
        private val addressBitWidth = PropertyImpl("element.property.addressBitWidth", BitWidth::class.java)
        private val dataBitWidth = PropertyImpl("element.property.dataBitWidth", BitWidth::class.java)
        private val text = PropertyImpl("graph.property.label", String::class.java)
    }

    override fun addProperties(bean: ROMView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        addressBitWidth.bind(editor, { bean.addressWidth }, { bean.addressWidth = it!!})
        dataBitWidth.bind(editor, { bean.dataWidth }, { bean.dataWidth = it!!})
        text.bind(editor, { bean.text}, { bean.text = it })

        properties.add(addressBitWidth)
        properties.add(dataBitWidth)
        properties.add(text)
    }
}