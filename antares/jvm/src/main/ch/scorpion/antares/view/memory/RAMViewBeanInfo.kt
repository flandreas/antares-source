package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl

/**
 * A [BeanInfo] for [RAMView].
 */
class RAMViewBeanInfo : DigitalComponentBeanInfo<RAMView>() {

    companion object {
        private val addressBitWidth = PropertyImpl("element.property.addressBitWidth", BitWidth::class.java)
        private val dataBitWidth = PropertyImpl("element.property.dataBitWidth", BitWidth::class.java)
        private val clock = PropertyImpl("element.property.RAM.clock", Boolean::class.java)
    }

    override fun addProperties(bean: RAMView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        addressBitWidth.bind(editor, { bean.addressWidth }, { bean.addressWidth = it!! })
        dataBitWidth.bind(editor, { bean.dataWidth}, { bean.dataWidth = it!! })
        clock.bind(editor, { bean.hasClock}, { bean.hasClock = it!! })

        properties.add(addressBitWidth)
        properties.add(dataBitWidth)
        properties.add(clock)
    }
}