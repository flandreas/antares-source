package ch.scorpion.antares.property

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.view.app.ChangeInputCountCommandJs
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import react.RBuilder


val jmInputCountField = enumPropertyField("InputCount", PortCount.values()) {
	props, newValue -> ChangeInputCountCommandJs(
		props.editor, props.beanProvider, props.beanIds, newValue, props.getter)
}

fun RBuilder.jmInputCount(
	editor: Editor,
	getter: PropertyGetter<PortCount>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<PortCount>.() -> Unit = {}
) = child(jmInputCountField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.beanIds = listOf(beanId.toString())
		this.beanProvider = beanProvider
		handler()
	}
}