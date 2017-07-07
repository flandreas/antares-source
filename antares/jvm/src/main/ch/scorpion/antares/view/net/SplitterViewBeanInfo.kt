package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.model.signal.BitWidth
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl

/**
 * A [BeanInfo] for [SplitterView].
 */
class SplitterViewBeanInfo : DigitalComponentBeanInfo<SplitterView>() {

    companion object {
        private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
        private val branchCount = PropertyImpl("element.property.branchCount", Int::class.java)
        private val handedness = PropertyImpl("element.property.Splitter.handedness", Handedness::class.java)
    }

    override fun addProperties(bean: SplitterView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model!!.isConnected

        bitWidth.bind(editor, { bean.bitWidth }, {bean.bitWidth = it!! }, !connected)
        branchCount.bind(editor, { bean.branchCount }, { bean.branchCount = it!! }, !connected)
        handedness.bind(editor, { bean.handedness }, { bean.handedness = it!! }, !connected)

        properties.add(bitWidth)
        properties.add(branchCount)
        properties.add(handedness)
    }
}