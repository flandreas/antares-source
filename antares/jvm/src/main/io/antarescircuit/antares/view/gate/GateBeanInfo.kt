package io.antarescircuit.antares.view.gate

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

open class BoxGateViewBeanInfo<T : BoxGateView<*>> : DigitalComponentViewBeanInfo<T>()

@Suppress("unused")
class DelayGateViewBeanInfo : VerticeViewBeanInfo<DelayGateView>() {

	companion object {
		private val delay = CommandPropertySwing("delay", "element.property.DelayGate.delay", Long::class.java, componentBeanProvider)
		private val bitWidth = AntaresProperties.bitWidth()
		private val orientation = EditProperties.orientation()
	}

	// Use special "delay" property
	override val isShowPropagationDelay: Boolean get() = false

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
		private val size = EditProperties.size()
	}

	override fun addProperties(bean: TriStateBufferGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(size.bind(editor, beanIdProvider(bean.id)))
		properties.add(enableLogic.bind(editor, beanIdProvider(bean.id)))
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(inputPortName.bind(editor, beanIdProvider(bean.id)))
		properties.add(outputPortName.bind(editor, beanIdProvider(bean.id)))
		properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
	}
}
