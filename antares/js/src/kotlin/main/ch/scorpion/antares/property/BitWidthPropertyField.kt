package ch.scorpion.antares.property

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import react.RBuilder
import react.child

val jmBitWidthField = enumPropertyField("BitWidth", BitWidth.values())

fun RBuilder.jmBitWidthField(
	editor: Editor,
	getter: PropertyGetter<BitWidth>,
	setter: PropertySetter<BitWidth>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<BitWidth>.() -> Unit = {}
) = child(jmBitWidthField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}