package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputPortNumber
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.view.DigitalGateViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.ComponentBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class AndGateViewBeanInfo : DigitalGateViewBeanInfo<AndGateView>() {
	companion object {
		val dataPort = CommandPropertySwing("dataPort", "element.property.AndGate.dataPort", InputPortNumber::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: AndGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(dataPort.bind(editor, bean.id, filter = { it.id <= bean.chosenInputCount.count }))
	}
}

@Suppress("unused")
class BufferGateViewBeanInfo : DigitalComponentBeanInfo<BufferGateView>() {
	companion object {
		val bitWidth = AntaresProperties.bitWidth()
	}

	override fun addProperties(bean: BufferGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, bean.id))
	}
}

@Suppress("unused")
class DelayGateViewBeanInfo : ComponentBeanInfo<DelayGateView>() {
	companion object {
		private val delay = CommandPropertySwing("delay", "element.property.DelayGate.delay", Long::class.java, componentBeanProvider)
		private val bitWidth = AntaresProperties.bitWidth()
		private val orientation = EditProperties.orientation()
	}

	override fun addProperties(bean: DelayGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(delay.bind(editor, bean.id))
		properties.add(bitWidth.bind(editor, bean.id))
		properties.add(orientation.bind(editor, bean.id))
	}
}

@Suppress("unused")
class NandGateViewBeanInfo : DigitalGateViewBeanInfo<NandGateView>()

@Suppress("unused")
class NorGateViewBeanInfo : DigitalGateViewBeanInfo<NorGateView>()

@Suppress("unused")
class NotGateViewBeanInfo : DigitalComponentBeanInfo<NotGateView>() {
	companion object {
		val bitWidth = AntaresProperties.bitWidth()
	}

	override fun addProperties(bean: NotGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(bitWidth.bind(editor, bean.id))
	}
}

@Suppress("unused")
class OrGateViewBeanInfo : DigitalGateViewBeanInfo<OrGateView>()

@Suppress("unused")
class TriStateBufferGateViewBeanInfo : DigitalComponentBeanInfo<TriStateBufferGateView>() {
	companion object {
		val enableLogic = CommandPropertySwing("enableLogic", "element.property.logic", Logic::class.java, componentBeanProvider)
		val bitWidth = AntaresProperties.bitWidth()
		val handedness = AntaresProperties.handedness(baseKey = "element.property.TriStateBuffer.handedness")
	}

	override fun addProperties(bean: TriStateBufferGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(enableLogic.bind(editor, bean.id))
		properties.add(bitWidth.bind(editor, bean.id))
		properties.add(handedness.bind(editor, bean.id))
	}
}

@Suppress("unused")
class XnorGateViewBeanInfo : DigitalGateViewBeanInfo<XnorGateView>()

@Suppress("unused")
class XorGateViewBeanInfo : DigitalGateViewBeanInfo<XorGateView>()