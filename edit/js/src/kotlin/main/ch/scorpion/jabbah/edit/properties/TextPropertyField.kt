package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.*
import com.ccfraser.muirwik.components.input.MInputMargin
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.input.margin
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.child
import react.functionalComponent
import react.useState

val jmTextField = functionalComponent<PropertyProps<String>>("TextField") { props ->
	var oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
	var (value, setValue) = useState(oldValue)
	mInput(value, disabled = props.disabled, fullWidth = false, onChange = { setValue((it.target as HTMLInputElement).value) }) {
		oldValue = value
		attrs.onBlur = { submitCommand(props, oldValue, value) }
		attrs.margin = MInputMargin.dense
	}
}

fun RBuilder.jmTextField(
	editor: Editor,
	getter: PropertyGetter<String>,
	setter: PropertySetter<String>,
	beanId: Int,
	disabled: Boolean = false,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<String>.() -> Unit = {}
) = child(jmTextField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.disabled = disabled
		this.beanProvider = beanProvider
		handler()
	}
}

fun RBuilder.jmReadOnlyTextField(
	editor: Editor,
	getter: PropertyGetter<String>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<String>.() -> Unit = {}
) = jmTextField(editor, getter, { _, _ -> } , beanId, disabled = true, beanProvider, handler)
