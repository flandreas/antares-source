package ch.scorpion.antares.property

import ch.scorpion.antares.model.InputCount
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import react.RBuilder
import react.child


val jmInputCountField = enumPropertyField("InputCount", InputCount.values())

fun RBuilder.jmInputCount(
	editor: Editor,
	getter: PropertyGetter<InputCount>,
	setter: PropertySetter<InputCount>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<InputCount>.() -> Unit = {}
) = child(jmInputCountField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}