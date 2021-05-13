package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.edit.*
import react.*

val jmDirectionField = enumPropertyField("Direction", Direction.values())

fun RBuilder.jmDirectionField(
	editor: Editor,
	getter: PropertyGetter<Direction>,
	setter: PropertySetter<Direction>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<Direction>.() -> Unit = {}
) = child(jmDirectionField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}