package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.view.Handedness
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation



/**
 * A [BeanInfo] for [ConcentratorView].
 */
class ConcentratorViewBeanInfo : DigitalComponentBeanInfo<ConcentratorView>() {

    companion object {
        private val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
        private val branchCount = PropertyImpl("element.property.branchCount", BranchCount::class.java)
        private val handedness = PropertyImpl("element.property.Splitter.handedness", Handedness::class.java)
        private val signalRep = PropertyImpl("element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java)
    }

    override fun addProperties(bean: ConcentratorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model.isConnected

        bitWidth.bind(editor, { bean.bitWidth }, {bean.bitWidth = it!! }, !connected)
        branchCount.bind(editor, { bean.branchCount }, { bean.branchCount = it!! }, !connected, { bean.model.supportedBranchCounts.contains(it)} )
        handedness.bind(editor, { bean.handedness }, { bean.handedness = it!! }, !connected)
        signalRep.bind(editor, { bean.signalRepresentation }, { bean.signalRepresentation = it!! })

        properties.add(bitWidth)
        properties.add(branchCount)
        properties.add(handedness)
        properties.add(signalRep)
    }
}