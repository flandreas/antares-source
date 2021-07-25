package ch.scorpion.antares.property

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.text.description.BASE_KEY_DESCRIPTION
import ch.scorpion.jabbah.edit.model.text.description.BASE_KEY_NAME
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.properties.*
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPage
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import com.ccfraser.muirwik.components.MGridProps
import styled.StyledElementBuilder

@Suppress("unused")
class CircuitInOutViewPropertyPage : ComponentPropertyPage<CircuitInOutView>() {

	override fun addProperties(bean: CircuitInOutView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			propertyRow(Component.BASE_KEY_ID) {
				it.jmReadOnlyTextField(editor, { bean.id.toString() }, bean.id)
			}

			propertyRow(AbstractGraphElementView.BASE_KEY_MODEL_ID) {
				it.jmReadOnlyTextField(editor, { bean.modelId.toString() }, bean.id)
			}

			propertyRow(BASE_KEY_NAME) {
				it.jmTextField(editor, { bean.name }, { _, value -> bean.name = value }, bean.id)
			}

			propertyRow(PortType.BASE_KEY) {
				it.jmPortTypeField(editor, { bean.portType }, { _, value -> bean.portType = value!! }, bean.id)
			}

			propertyRow(BitWidth.BASE_KEY) {
				it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
			}

			propertyRow(Component.BASE_KEY_ORIENTATION) {
				it.jmDirectionField(editor, { bean.orientation }, { _, value -> bean.orientation = value!! }, bean.id )
			}

			propertyRow(Stylable.BASE_KEY_CUSTOM_COLOR) {
				it.jmPredefinedColorField(editor, { bean.customColor }, { _, value -> bean.customColor = value }, bean.id )
			}

			propertyRow(DigitalSignalRepresentation.BASE_KEY) {
				it.jmDigitalSignalRepresentation(editor, { bean.signalRepresentation }, { _, value -> bean.signalRepresentation = value!! }, bean.id )
			}

			propertyRow(SwitchView.BASE_KEY_TOGGLE) {
				it.jmCheckboxField(editor, { bean.toggle }, { _, value -> bean.toggle = value!! }, bean.id)
			}

			propertyRow(BASE_KEY_DESCRIPTION) {
				it.jmMultilineTextField(editor, { bean.description.value }, { _, value -> bean.description = Description(value ?: "EMPTY") }, bean.id)
			}
		}
	}
}