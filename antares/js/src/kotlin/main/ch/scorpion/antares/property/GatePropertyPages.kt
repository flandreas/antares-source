package ch.scorpion.antares.property

import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.antares.view.gate.AbstractLogicGateView
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.gate.NotGateView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.jmCheckboxField
import ch.scorpion.jabbah.edit.properties.jmDirectionField
import ch.scorpion.jabbah.edit.properties.jmTextField
import ch.scorpion.jabbah.edit.properties.propertyRow
import ch.scorpion.jabbah.graph.ui.property.VerticeViewPropertyPage
import com.ccfraser.muirwik.components.MGridAlignItems
import com.ccfraser.muirwik.components.MGridProps
import com.ccfraser.muirwik.components.MGridSpacing
import com.ccfraser.muirwik.components.mGridContainer
import react.RBuilder
import styled.StyledElementBuilder

abstract class DigitalComponentPropertyPage<T : DigitalComponentView<*>> : VerticeViewPropertyPage<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.propertyRow("edit.property.Component.orientation.name") {
			it.jmDirectionField(editor, { bean.orientation }, { _, value -> bean.orientation = value!! }, bean.id )
		}
	}
}

abstract class LogicGateViewPropertyPage<T : AbstractLogicGateView<*>> : DigitalComponentPropertyPage<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			// TODO: Support for properties without setter that instead create a Command (for calling a service)
			/*
			propertyRow("element.property.inputCount") {
				it.jmInputCount(editor, { bean.chosenInputCount}, { _, value -> bean.chosenInputCount = value!!}, bean.id)
			}
			*/
			propertyRow("element.property.outputPort.name") {
				it.jmTextField(editor, { bean.outputPortName }, { _, value -> bean.outputPortName = value }, bean.id)
			}
			for (i in 1..bean.chosenInputCount.count) {
				propertyRow("element.property.Gate.negateInput${i + 1}.name") {
					it.jmCheckboxField(editor, { bean.getInputNegation(i) }, { _, value -> bean.setInputNegation(i, value!!)}, bean.id)
				}
			}
		}
	}
}

class AndGateViewPropertyPage : LogicGateViewPropertyPage<AndGateView>() {
	override fun render(bean: AndGateView, editor: Editor, builder: RBuilder) {
		builder.run {
			mGridContainer(MGridSpacing.spacing1, alignItems = MGridAlignItems.center) {
				addProperties(bean, editor, this)
				// TODO: Add dataPort property using filter
			}
		}
	}
}

class NotGateViewPropertyPage : DigitalComponentPropertyPage<NotGateView>() {

	override fun render(bean: NotGateView, editor: Editor, builder: RBuilder) {
		builder.run {
			mGridContainer(MGridSpacing.spacing1, alignItems = MGridAlignItems.center) {
				addProperties(bean, editor, this)

				propertyRow("element.property.bitWidth.name") {
					it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
				}
			}
		}
	}
}