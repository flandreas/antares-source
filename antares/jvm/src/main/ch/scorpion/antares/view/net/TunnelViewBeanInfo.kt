package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl

/**
 * A [BeanInfo] for [TunnelView].
 */
class TunnelViewBeanInfo : DigitalComponentBeanInfo<TunnelView>() {

    companion object {
        private val name = PropertyImpl("element.property", String::class.java)
        private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
    }

    override fun addProperties(bean: TunnelView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.name }, { bean.name = it })
        bitWidth.bind(editor, { bean.bitWidth }, { bean.bitWidth = it!! })

        properties.add(name)
        properties.add(bitWidth)
    }
}