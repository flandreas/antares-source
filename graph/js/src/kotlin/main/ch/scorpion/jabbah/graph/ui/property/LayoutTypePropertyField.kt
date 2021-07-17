package ch.scorpion.jabbah.graph.ui.property

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType
import react.RBuilder
import react.child

val jmLayoutTypeField = enumPropertyField("LayoutType", LayoutType.values())

fun RBuilder.jmLayoutTypeField(
	editor: Editor,
	getter: PropertyGetter<LayoutType>,
	setter: PropertySetter<LayoutType>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<LayoutType>.() -> Unit = {}
) = child(jmLayoutTypeField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}