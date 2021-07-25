package ch.scorpion.antares.property

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.gate.*
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

open class DigitalComponentPropertyPage<T : DigitalComponentView<*>> : VerticeViewPropertyPage<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.propertyRow(Component.BASE_KEY_ORIENTATION) {
			it.jmDirectionField(editor, { bean.orientation }, { _, value -> bean.orientation = value!! }, bean.id )
		}
	}
}

open class LogicGateViewPropertyPage<T : AbstractLogicGateView<*>> : DigitalComponentPropertyPage<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			propertyRow(InputCount.BASE_KEY) {
				it.jmInputCount(editor, { bean.chosenInputCount}, bean.id) {
					filter = { inputCount -> inputCount.ordinal >= 2 }
				}
			}
			propertyRow(AbstractDigitalGateView.BASE_KEY_OUTPUT_PORT_NAME) {
				it.jmTextField(editor, { bean.outputPortName }, { _, value -> bean.outputPortName = value }, bean.id)
			}
			for (i in 1..bean.chosenInputCount.count) {
				propertyRow("${AbstractLogicGateView.BASE_KEY_NEGATE_INPUT}${i + 1}") {
					it.jmCheckboxField(editor, { bean.getInputNegation(i) }, { _, value -> bean.setInputNegation(i, value!!)}, bean.id)
				}
			}
		}
	}
}

class AndGateViewPropertyPage : LogicGateViewPropertyPage<AndGateView>() {
	override fun addProperties(bean: AndGateView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.propertyRow(AndGateView.BASE_KEY_DATA_PORT) {
			it.jmInputPortNumber(editor, { bean.dataPort}, { _, value -> bean.dataPort = value!!}, bean.id) {
				filter = { inputPortNumber -> inputPortNumber.id <= bean.chosenInputCount.count }
			}
		}
	}
}

class BufferGateViewPropertyPage : DigitalComponentPropertyPage<BufferGateView>() {
	override fun addProperties(bean: BufferGateView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.propertyRow(BitWidth.BASE_KEY) {
			it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
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

class NotGateViewPropertyPage : DigitalComponentPropertyPage<NotGateView>() {
	override fun addProperties(bean: NotGateView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.propertyRow(BitWidth.BASE_KEY) {
			it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
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