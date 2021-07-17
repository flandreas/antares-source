package ch.scorpion.antares.property

import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.properties.*
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPage
import com.ccfraser.muirwik.components.MGridProps
import styled.StyledElementBuilder

@Suppress("unused")
class CircuitInOutViewPropertyPage : ComponentPropertyPage<CircuitInOutView>() {

	override fun addProperties(bean: CircuitInOutView, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			propertyRow("edit.property.id.name") {
				it.jmReadOnlyTextField(editor, { bean.id.toString() }, bean.id)
			}

			propertyRow("graph.property.modelId.name") {
				it.jmReadOnlyTextField(editor, { bean.modelId.toString() }, bean.id)
			}

			propertyRow("edit.property.name.name") {
				it.jmTextField(editor, { bean.name }, { _, value -> bean.name = value }, bean.id)
			}

			propertyRow("graph.property.portType.name") {
				it.jmPortTypeField(editor, { bean.portType }, { _, value -> bean.portType = value!! }, bean.id)
			}

			propertyRow("element.property.bitWidth.name") {
				it.jmBitWidthField(editor, { bean.bitWidth }, { _, value -> bean.bitWidth = value!! }, bean.id )
			}

			propertyRow("edit.property.Component.orientation.name") {
				it.jmDirectionField(editor, { bean.orientation }, { _, value -> bean.orientation = value!! }, bean.id )
			}

			propertyRow("edit.property.color.name") {
				it.jmPredefinedColorField(editor, { bean.customColor }, { _, value -> bean.customColor = value }, bean.id )
			}

			propertyRow("element.property.DigitalSignalRepresentation.name") {
				it.jmDigitalSignalRepresentation(editor, { bean.signalRepresentation }, { _, value -> bean.signalRepresentation = value!! }, bean.id )
			}

			propertyRow("element.property.Switch.toggle.name") {
				it.jmCheckboxField(editor, { bean.toggle }, { _, value -> bean.toggle = value!! }, bean.id)
			}

			propertyRow("edit.property.description.name") {
				it.jmMultilineTextField(editor, { bean.description.value }, { _, value -> bean.description = Description(value ?: "EMPTY") }, bean.id)
			}
		}
	}
}