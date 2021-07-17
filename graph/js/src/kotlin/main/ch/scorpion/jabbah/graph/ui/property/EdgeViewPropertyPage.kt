package ch.scorpion.jabbah.graph.ui.property

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.jmCheckboxField
import ch.scorpion.jabbah.edit.properties.jmReadOnlyTextField
import ch.scorpion.jabbah.edit.properties.propertyRow
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPage
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import com.ccfraser.muirwik.components.MGridAlignItems
import com.ccfraser.muirwik.components.MGridSpacing
import com.ccfraser.muirwik.components.mGridContainer
import react.RBuilder

class EdgeViewPropertyPage : ComponentPropertyPage<EdgeViewImpl<*>>() {

	override fun render(bean: EdgeViewImpl<*>, editor: Editor, builder: RBuilder) {
		builder.run {
			mGridContainer(MGridSpacing.spacing1, alignItems = MGridAlignItems.center) {
				addProperties(bean, editor, this)

				propertyRow("graph.property.modelId.name") {
					it.jmReadOnlyTextField(editor, { bean.modelId.toString() }, bean.id)
				}
				propertyRow("graph.property.edgeView.arrow.name") {
					it.jmCheckboxField(editor, { bean.isArrow}, { _, value -> bean.isArrow = value!! }, bean.id)
				}
				propertyRow("graph.property.edgeView.layout.name") {
					it.jmLayoutTypeField(editor, { bean.layout.type}, { _, value -> bean.layout.type = value!!}, bean.id)
				}
				propertyRow("graph.property.edgeViewLineStyle.name") {
					it.jmNetViewStyleField(editor, { bean.netView!!.style}, { _, value -> bean.netView!!.style = value!!}, bean.id)
				}
			}
		}
	}
}