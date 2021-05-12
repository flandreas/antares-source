package ch.scorpion.jabbah.edit.properties

import com.ccfraser.muirwik.components.input.MInputMargin
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.input.margin
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.child
import react.functionalComponent
import react.useState

val jmTextField = functionalComponent<PropertyProps<String>>("TextField") { props ->
	val oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
	var (value, setValue) = useState(oldValue)
	mInput(value, disabled = props.disabled, fullWidth = false, onChange = { setValue((it.target as HTMLInputElement).value) }) {
		attrs.onBlur = { submitCommand(props, oldValue, value) }
		attrs.margin = MInputMargin.dense
	}
}

fun RBuilder.jmTextField(handler: PropertyProps<String>.() -> Unit) = child(jmTextField) {
	attrs {
		handler()
	}
}
