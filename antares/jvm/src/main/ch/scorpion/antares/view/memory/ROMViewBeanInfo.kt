package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/**
 * A [BeanInfo] for [ROMView].
 */
@Suppress("unused")
class ROMViewBeanInfo : DigitalComponentBeanInfo<ROMView>() {

    companion object {
        private val addressBitWidth = PropertyImpl("element.property.addressBitWidth", BitWidth::class.java)
        private val dataBitWidth = PropertyImpl("element.property.dataBitWidth", BitWidth::class.java)
        private val text = PropertyImpl("graph.property.label", String::class.java)
        private val showContents = PropertyImpl("element.property.Addressable.showContents", Boolean::class.java)
        private val contentRowsCount = PropertyImpl("element.property.Addressable.rowsCount", Int::class.java)
        private val contentColumnsCount = PropertyImpl("element.property.Addressable.columnsCount", Int::class.java)
        private val disassemblerConfig = PropertyImpl("element.property.ROM.disassemblerConfig", TextProperty::class.java)
        private val showDisassembler = PropertyImpl("element.property.ROM.disassemblerDisplay", Boolean::class.java)
    }

    override fun addProperties(bean: ROMView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        addressBitWidth.bind(editor, { bean.addressWidth }, { bean.addressWidth = it!!})
        dataBitWidth.bind(editor, { bean.dataWidth }, { bean.dataWidth = it!!})
        text.bind(editor, { bean.text}, { bean.text = it })
        showContents.bind(editor, { bean.showContents }, { bean.showContents = it!! })
        if (bean.showContents) {
            contentRowsCount.bind(editor, { bean.contentRowsCount }, { bean.contentRowsCount = it!! })
            contentColumnsCount.bind(editor, { bean.contentColumnsCount }, { bean.contentColumnsCount = it!! })
            disassemblerConfig.bind(editor, { bean.disassemblerConfig }, { bean.disassemblerConfig = it!! })
            showDisassembler.bind(editor, { bean.showDisassembler }, { bean.showDisassembler = it!! })
        }

        properties.add(addressBitWidth)
        properties.add(dataBitWidth)
        properties.add(text)
        properties.add(showContents)
        if (bean.showContents) {
            properties.add(contentRowsCount)
            properties.add(contentColumnsCount)
            properties.add(disassemblerConfig)
            properties.add(showDisassembler)
        }
    }
}