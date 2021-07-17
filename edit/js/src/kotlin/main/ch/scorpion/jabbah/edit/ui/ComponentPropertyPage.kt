package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractPropertyPageRenderer
import ch.scorpion.jabbah.edit.properties.jmReadOnlyTextField
import ch.scorpion.jabbah.edit.properties.propertyRow
import com.ccfraser.muirwik.components.MGridProps
import styled.StyledElementBuilder

open class ComponentPropertyPage<T : Component> : AbstractPropertyPageRenderer<T>() {
	override fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) {
		builder.propertyRow("edit.property.id.name") {
			it.jmReadOnlyTextField(editor, { bean.id.toString() }, bean.id)
		}
	}
}