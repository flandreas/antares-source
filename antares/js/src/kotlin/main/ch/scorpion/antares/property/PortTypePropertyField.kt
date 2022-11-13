package ch.scorpion.antares.property

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import ch.scorpion.jabbah.graph.model.PortType
import react.RBuilder

val jmPortTypeField = enumPropertyField("PortType", PortType.values())

fun RBuilder.jmPortTypeField(
	editor: Editor,
	getter: PropertyGetter<PortType>,
	setter: PropertySetter<PortType>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<PortType>.() -> Unit = {}
) = child(jmPortTypeField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId.toString())
		this.beanProvider = beanProvider
		handler()
	}
}