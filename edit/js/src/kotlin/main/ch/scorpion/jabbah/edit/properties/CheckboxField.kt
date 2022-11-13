package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.*
import com.ccfraser.muirwik.components.mCheckbox
import react.RBuilder
import react.*

val jmCheckboxField = functionalComponent<PropertyProps<Boolean>> { props ->
	var oldValue = props.getter(props.beanProvider(props.editor, props.beanIds.map { it.toString() }))
	val (value, setValue) = useState(oldValue)
	mCheckbox(
		value ?: false,
		disabled = props.disabled,
		onChange = { _, b ->
			setValue(b)
			submitCommand(props, b)
			oldValue = b
		})
}

fun RBuilder.jmCheckboxField(
	editor: Editor,
	getter: PropertyGetter<Boolean>,
	setter: PropertySetter<Boolean>,
	beanId: Int,
	disabled: Boolean = false,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<Boolean>.() -> Unit = {}
) = child(jmCheckboxField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId.toString())
		this.disabled = disabled
		this.beanProvider = beanProvider
		handler()
	}
}