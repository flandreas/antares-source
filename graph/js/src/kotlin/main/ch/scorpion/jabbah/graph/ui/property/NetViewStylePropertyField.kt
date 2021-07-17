package ch.scorpion.jabbah.graph.ui.property

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.enumPropertyField
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import react.RBuilder
import react.child

val jmNetViewStyleField = enumPropertyField("NetViewStyle", NetViewStyle.values())

fun RBuilder.jmNetViewStyleField(
	editor: Editor,
	getter: PropertyGetter<NetViewStyle>,
	setter: PropertySetter<NetViewStyle>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<NetViewStyle>.() -> Unit = {}
) = child(jmNetViewStyleField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}