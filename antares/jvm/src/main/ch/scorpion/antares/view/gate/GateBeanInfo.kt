package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputPortNumber
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.AndGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalGateViewBeanInfo
import ch.scorpion.antares.view.Handedness
import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.base.geom.Direction

@Suppress("unused") class AndGateViewBeanInfo : DigitalGateViewBeanInfo<AndGateView>() {
    companion object {
        val dataPort = PropertyImpl("element.property.AndGate.dataPort", InputPortNumber::class.java)
    }

    override fun addProperties(bean: AndGateView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        dataPort.bind(editor, {bean.dataPort}, {bean.dataPort = it!!}, true, { it.id <= bean.chosenInputCount.count })
        properties.add(dataPort)
    }
}

@Suppress("unused") class BufferGateViewBeanInfo : DigitalComponentBeanInfo<BufferGateView>() {
    companion object {
        val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
    }

    override fun addProperties(bean: BufferGateView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)
        bitWidth.bind(editor, {bean.bitWidth}, {bean.bitWidth = it!!})
        properties.add(bitWidth)
    }
}

@Suppress("unused") class DelayGateViewBeanInfo : AbstractBeanInfo<DelayGateView>() {
    companion object {
        val id = PropertyImpl("edit.property.id", Integer::class.java)
        val delay = PropertyImpl("element.property.DelayGate.delay", Long::class.java)
        val orientation = PropertyImpl("edit.property.Component.orientation", Direction::class.java)
    }

    override fun addProperties(bean: DelayGateView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        id.bind(editor, {Integer(bean.id)}, null, false)
        delay.bind(editor, {bean.delay}, {bean.delay = it!!})
        orientation.bind(editor, {bean.orientation}, {bean.orientation = it!!})

        properties.add(id)
        properties.add(delay)
        properties.add(orientation)
    }
}

@Suppress("unused") class NandGateViewBeanInfo : DigitalGateViewBeanInfo<NandGateView>()

@Suppress("unused") class NorGateViewBeanInfo : DigitalGateViewBeanInfo<NorGateView>()

@Suppress("unused") class NotGateViewBeanInfo : DigitalComponentBeanInfo<NotGateView>()

@Suppress("unused") class OrGateViewBeanInfo : DigitalGateViewBeanInfo<OrGateView>()

@Suppress("unused") class TriStateBufferGateViewBeanInfo : DigitalComponentBeanInfo<TriStateBufferGateView>() {
    companion object {
        val enableLogic = PropertyImpl("element.property.logic", Logic::class.java)
        val bitWidth = PropertyImpl("element.property.bitWidth", BitWidth::class.java)
        val handedness = PropertyImpl("element.property.TriStateBuffer.handedness", Handedness::class.java)
    }

    override fun addProperties(bean: TriStateBufferGateView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        enableLogic.bind(editor, {bean.enableLogic}, {bean.enableLogic = it!!})
        bitWidth.bind(editor, {bean.bitWidth}, { bean.bitWidth = it!!})
        handedness.bind(editor, {bean.handedness}, {bean.handedness = it!!})

        properties.add(enableLogic)
        properties.add(bitWidth)
        properties.add(handedness)
    }
}

@Suppress("unused") class XnorGateViewBeanInfo : DigitalGateViewBeanInfo<XnorGateView>()

@Suppress("unused") class XorGateViewBeanInfo : DigitalGateViewBeanInfo<XnorGateView>()