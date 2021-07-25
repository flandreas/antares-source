package ch.scorpion.jabbah.graph.ui.property

import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.text.description.BASE_KEY_DESCRIPTION
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.properties.*
import ch.scorpion.jabbah.edit.ui.ComponentPropertyPage
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import com.ccfraser.muirwik.components.MGridProps
import styled.StyledElementBuilder

open class VerticeViewPropertyPage<T : AbstractVerticeView<*>> : ComponentPropertyPage<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		super.addProperties(bean, editor, builder)
		builder.run {
			propertyRow(AbstractGraphElementView.BASE_KEY_MODEL_ID) {
				it.jmReadOnlyTextField(editor, { bean.modelId.toString() }, bean.id)
			}
			propertyRow(AbstractGraphElementView.BASE_KEY_PROPAGATION_DELAY) {
				it.jmReadOnlyTextField(editor, { bean.propagationDelay.toString() }, bean.id)
			}
			propertyRow(Stylable.BASE_KEY_CUSTOM_COLOR) {
				it.jmPredefinedColorField(editor, { bean.customColor }, { _, value -> bean.customColor = value }, bean.id )
			}
			propertyRow(BASE_KEY_DESCRIPTION) {
				it.jmMultilineTextField(editor, { bean.description.value }, { _, value -> bean.description = Description(value ?: "EMPTY") }, bean.id)
			}
			propertyRow(Stylable.BASE_KEY_SHADOW) {
				it.jmCheckboxField(editor, { bean.shadow }, { _, value -> bean.customShadow = value!! }, bean.id)
			}
		}
	}
}