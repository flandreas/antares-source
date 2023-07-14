package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputPortNumber
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.app.InputCountPropertySwing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class LogicGateViewBeanInfo  : BoxGateViewBeanInfo<LogicGateView>() {

	companion object {
		private val inputCount = InputCountPropertySwing(componentBeanProvider)
		private val bitWidth = AntaresProperties.bitWidth()
		private val outputPortName = CommandPropertySwing("outputPortName", LogicGateView.BASE_KEY_OUTPUT_PORT_NAME, String::class.java, componentBeanProvider)
		private val negateInput = Array(8) { portId ->
			CommandPropertySwing("negateInput${portId + 1}", "${LogicGateView.BASE_KEY_NEGATE_INPUT}${portId + 1}", Boolean::class.java, componentBeanProvider)
		}
		private val inputPortName = Array(8) { portId ->
			CommandPropertySwing("inputPortName${portId + 1}", "${LogicGateView.BASE_KEY_INPUT_PORT_NAME}${portId + 1}", String::class.java, componentBeanProvider)
		}
		val dataPort = CommandPropertySwing("dataPort", LogicGateView.BASE_KEY_DATA_PORT, InputPortNumber::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: LogicGateView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		if (bean.model.maxInputCount.count > 1) {
			properties.add(inputCount.bind(editor, beanIdProvider(bean.id), editable = true, filter = { it.ordinal >= 2 }))
		}
		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(outputPortName.bind(editor, beanIdProvider(bean.id)))

		if (bean.model.logicGateType == NonUnaryLogicGateType.And) {
			properties.add(dataPort.bind(editor, beanIdProvider(bean.id), filter = { it.id <= bean.chosenInputCount.count }))
		}

		for (i in 0 until bean.chosenInputCount.count) {
			properties.add(inputPortName[i].bind(editor, beanIdProvider(bean.id)))
			properties.add(negateInput[i].bind(editor, beanIdProvider(bean.id)))
		}
	}
}