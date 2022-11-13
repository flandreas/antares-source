package ch.scorpion.antares.property

import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import react.RBuilder

val jmSignalRepresentationField = enumPropertyField("SignalRepresentation", DigitalSignalRepresentation.values())

fun RBuilder.jmDigitalSignalRepresentation(
	editor: Editor,
	getter: PropertyGetter<DigitalSignalRepresentation>,
	setter: PropertySetter<DigitalSignalRepresentation>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<DigitalSignalRepresentation>.() -> Unit = {}
) = child(jmSignalRepresentationField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId.toString())
		this.beanProvider = beanProvider
		handler()
	}
}