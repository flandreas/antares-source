package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.AbstractComponentBeanInfo
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

open class BoxGateViewBeanInfo<T : BoxGateView<*>> : DigitalComponentViewBeanInfo<T>()

@Suppress("unused")
class DelayGateViewBeanInfo : AbstractComponentBeanInfo<DelayGateView>() {
	companion object {
		private val delay = CommandPropertySwing("delay", "element.property.DelayGate.delay", Long::class.java, componentBeanProvider)
		private val bitWidth = AntaresProperties.bitWidth()
		private val orientation = EditProperties.orientation()
	}

	override fun addProperties(bean: DelayGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(delay.bind(editor, beanIdProvider(bean.id)))
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(orientation.bind(editor, beanIdProvider(bean.id)))
	}
}

@Suppress("unused")
class TriStateBufferGateViewBeanInfo : DigitalComponentViewBeanInfo<TriStateBufferGateView>() {

	companion object {
		val enableLogic = CommandPropertySwing("enableLogic", Logic.BASE_KEY, Logic::class.java, componentBeanProvider)
		private val bitWidth = AntaresProperties.bitWidth()
		val handedness = AntaresProperties.handedness(baseKey = Handedness.BASE_KEY)
		private val inputPortName = AntaresProperties.inputPortName(portId = 1)
		private val outputPortName = AntaresProperties.outputPortName()
	}

	override fun addProperties(bean: TriStateBufferGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(enableLogic.bind(editor, beanIdProvider(bean.id)))
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(inputPortName.bind(editor, beanIdProvider(bean.id)))
		properties.add(outputPortName.bind(editor, beanIdProvider(bean.id)))
		properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
	}
}
