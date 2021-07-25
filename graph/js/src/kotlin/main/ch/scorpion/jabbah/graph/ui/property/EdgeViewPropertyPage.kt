package ch.scorpion.jabbah.graph.ui.property

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.jmCheckboxField
import ch.scorpion.jabbah.edit.properties.jmReadOnlyTextField
import ch.scorpion.jabbah.edit.properties.propertyRow
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPage
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import com.ccfraser.muirwik.components.MGridProps
import styled.StyledElementBuilder

class EdgeViewPropertyPage : ComponentPropertyPage<EdgeViewImpl<*>>() {

	override fun addProperties(bean: EdgeViewImpl<*>, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			propertyRow(AbstractGraphElementView.BASE_KEY_MODEL_ID) {
				it.jmReadOnlyTextField(editor, { bean.modelId.toString() }, bean.id)
			}
			propertyRow(EdgeView.BASE_KEY_ARROW) {
				it.jmCheckboxField(editor, { bean.isArrow}, { _, value -> bean.isArrow = value!! }, bean.id)
			}
			propertyRow(EdgeView.BASE_KEY_LAYOUT) {
				it.jmLayoutTypeField(editor, { bean.layout.type}, { _, value -> bean.layout.type = value!!}, bean.id)
			}
			propertyRow(EdgeView.BASE_KEY_LINE_STYLE) {
				it.jmNetViewStyleField(editor, { bean.netView!!.style}, { _, value -> bean.netView!!.style = value!!}, bean.id)
			}
		}
	}
}