package ch.scorpion.jabbah.graph.ui.property

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.properties.*
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPage
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import com.ccfraser.muirwik.components.MGridProps
import styled.StyledElementBuilder

open class VerticeViewPropertyPage<T : AbstractVerticeView<*>> : ComponentPropertyPage<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			propertyRow("graph.property.modelId.name") {
				it.jmReadOnlyTextField(editor, { bean.modelId.toString() }, bean.id)
			}
			propertyRow("element.property.propagationDelay.name") {
				it.jmReadOnlyTextField(editor, { bean.propagationDelay.toString() }, bean.id)
			}
			propertyRow("edit.property.color.name") {
				it.jmPredefinedColorField(editor, { bean.customColor }, { _, value -> bean.customColor = value }, bean.id )
			}
			propertyRow("edit.property.description.name") {
				it.jmMultilineTextField(editor, { bean.description.value }, { _, value -> bean.description = Description(value ?: "EMPTY") }, bean.id)
			}
			propertyRow("edit.property.shadow.name") {
				it.jmCheckboxField(editor, { bean.shadow }, { _, value -> bean.customShadow = value!! }, bean.id)
			}
		}
	}
}