package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.*
import com.ccfraser.muirwik.components.input.MInputMargin
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.input.margin
import com.ccfraser.muirwik.components.targetInputValue
import react.RBuilder
import react.child
import react.functionalComponent
import react.useState

interface TextPropertyProps<T> : PropertyProps<T> {
	var multiline: Boolean
	var rows: Int?
	var rowsMax: Int?
}

val jmTextField = functionalComponent<TextPropertyProps<String>>("TextField") { props ->
	var oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
	val (value, setValue) = useState(oldValue)
	mInput(
		value,
		disabled = props.disabled,
		fullWidth = false,
		multiline = props.multiline,
		rows = props.rows,
		rowsMax = props.rowsMax,
		onChange = { setValue(it.targetInputValue) }
	) {
		oldValue = value
		attrs.onBlur = {
			submitCommand(props, value)
		}
		attrs.margin = MInputMargin.dense
	}
}

fun RBuilder.jmTextField(
	editor: Editor,
	getter: PropertyGetter<String>,
	setter: PropertySetter<String>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	disabled: Boolean = false,
	multiline: Boolean = false,
	rows: Int? = null,
	rowsMax: Int? = null,
	handler: PropertyProps<String>.() -> Unit = {}
) = child(jmTextField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.disabled = disabled
		this.beanProvider = beanProvider
		this.multiline = multiline
		this.rows = rows
		this.rowsMax = rowsMax
		handler()
	}
}

fun RBuilder.jmReadOnlyTextField(
	editor: Editor,
	getter: PropertyGetter<String>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<String>.() -> Unit = {}
) = jmTextField(editor, getter, { _, _ -> } , beanId, beanProvider, disabled = true, multiline = false, handler = handler)

fun RBuilder.jmMultilineTextField(
	editor: Editor,
	getter: PropertyGetter<String>,
	setter: PropertySetter<String>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<String>.() -> Unit = {}
) = jmTextField(editor, getter, setter, beanId, beanProvider, disabled = false, multiline = true, rows = 4, rowsMax = 4, handler = handler)
