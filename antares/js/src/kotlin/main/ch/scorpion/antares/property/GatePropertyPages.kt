package ch.scorpion.antares.property

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.gate.DelayGateView
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.jmCheckboxField
import ch.scorpion.jabbah.edit.properties.jmDirectionField
import ch.scorpion.jabbah.edit.properties.jmTextField
import ch.scorpion.jabbah.edit.properties.propertyRow
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPage
import ch.scorpion.jabbah.graph.ui.property.VerticeViewPropertyPage
import com.ccfraser.muirwik.components.MGridProps
import styled.StyledElementBuilder

open class DigitalComponentPropertyPage<T : OrientableRectangularVerticeView<*>> : VerticeViewPropertyPage<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.propertyRow(Component.BASE_KEY_ORIENTATION) {
			it.jmDirectionField(editor, { bean.orientation }, { _, value -> bean.orientation = value!! }, bean.id )
		}
	}
}

open class LogicGateViewPropertyPage : DigitalComponentPropertyPage<LogicGateView>() {
	override fun addProperties(bean: LogicGateView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.propertyRow(BitWidth.BASE_KEY) {
			it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
		}
		if (bean.model.gateType == NonUnaryLogicGateType.And) {
			builder.propertyRow(LogicGateView.BASE_KEY_DATA_PORT) {
				it.jmInputPortNumber(editor, { bean.dataPort}, { _, value -> bean.dataPort = value!!}, bean.id) {
					filter = { inputPortNumber -> inputPortNumber.id <= bean.chosenInputCount.count }
				}
			}
		}
		builder.run {
			propertyRow(PortCount.INPUT_COUNT_BASE_KEY) {
				it.jmInputCount(editor, { bean.chosenInputCount}, bean.id) {
					filter = { inputCount -> inputCount.ordinal >= 2 }
				}
			}
			propertyRow(LogicGateView.BASE_KEY_OUTPUT_PORT_NAME) {
				it.jmTextField(editor, { bean.outputPortName }, { _, value -> bean.outputPortName = value }, bean.id)
			}
			for (i in 1..bean.chosenInputCount.count) {
				propertyRow("${LogicGateView.BASE_KEY_NEGATE_INPUT}${i + 1}") {
					it.jmCheckboxField(editor, { bean.getInputNegation(i) }, { _, value -> bean.setInputNegation(i, value!!)}, bean.id)
				}
			}
		}
	}
}

class DelayGateViewPropertyPage : ComponentPropertyPage<DelayGateView>() {
	override fun addProperties(bean: DelayGateView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		// TODO Add delay property
		builder.run {
			propertyRow(BitWidth.BASE_KEY) {
				it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
			}
			propertyRow(Component.BASE_KEY_ORIENTATION) {
				it.jmDirectionField(editor, { bean.orientation }, { _, value -> bean.orientation = value!! }, bean.id )
			}
		}
	}
}


class TriStateBufferGateViewPropertyPage : DigitalComponentPropertyPage<TriStateBufferGateView>() {
	override fun addProperties(bean: TriStateBufferGateView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			propertyRow(Logic.BASE_KEY) {
				it.jmLogicField(editor, { bean.enableLogic }, { _, value -> bean.enableLogic = value!! }, bean.id)
			}
			propertyRow(BitWidth.BASE_KEY) {
				it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
			}
			propertyRow(Handedness.BASE_KEY) {
				it.jmHandednessField(editor, { bean.handedness }, { _, value -> bean.handedness = value!! }, bean.id)
			}
		}
	}
}