package ch.scorpion.antares.property

import ch.scorpion.antares.model.InputPortNumber
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import react.RBuilder

val jmInputPortNumberField = enumPropertyField("InputPortNumber", InputPortNumber.values())

fun RBuilder.jmInputPortNumber(
	editor: Editor,
	getter: PropertyGetter<InputPortNumber>,
	setter: PropertySetter<InputPortNumber>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<InputPortNumber>.() -> Unit = {}
) = child(jmInputPortNumberField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId.toString())
		this.beanProvider = beanProvider
		handler()
	}
}