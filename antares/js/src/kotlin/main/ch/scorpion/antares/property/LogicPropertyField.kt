package ch.scorpion.antares.property

import ch.scorpion.antares.model.Logic
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import react.RBuilder
import react.child

val jmLogicField = enumPropertyField("Logic", Logic.values())

fun RBuilder.jmLogicField(
	editor: Editor,
	getter: PropertyGetter<Logic>,
	setter: PropertySetter<Logic>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<Logic>.() -> Unit = {}
) = child(jmLogicField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}