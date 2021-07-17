package ch.scorpion.antares.property

import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import react.RBuilder
import react.child

val jmHandednessField = enumPropertyField("Handedness", Handedness.values())

fun RBuilder.jmHandednessField(
	editor: Editor,
	getter: PropertyGetter<Handedness>,
	setter: PropertySetter<Handedness>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<Handedness>.() -> Unit = {}
) = child(jmHandednessField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}