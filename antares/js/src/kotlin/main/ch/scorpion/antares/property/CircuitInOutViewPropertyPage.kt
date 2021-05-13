package ch.scorpion.antares.property

import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.*
import com.ccfraser.muirwik.components.*
import kotlinx.css.margin
import kotlinx.css.maxWidth
import kotlinx.css.minWidth
import kotlinx.css.px
import react.RBuilder
import styled.StyleSheet

@Suppress("unused")
class CircuitInOutViewPropertyPage : PropertyPageRenderer<CircuitInOutView> {

	private object ComponentStyles : StyleSheet("ComponentStyles", isStatic = true) {
		val formControl by css {
			margin(1.spacingUnits)
			minWidth = 120.px
			maxWidth = 300.px
		}
	}

	override fun render(bean: CircuitInOutView, editor: Editor, builder: RBuilder) {
		builder.run {
			mGridContainer(MGridSpacing.spacing1, alignItems = MGridAlignItems.center) {

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
			}
		}
	}
}