package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import com.ccfraser.muirwik.components.mTextField
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.child
import react.functionalComponent
import react.useState

val jmTextField = functionalComponent<PropertyProps<String>>("TextField") { props ->
	val oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
	var (value, setValue) = useState(oldValue)
	mTextField(Translations.getString("edit.property.name.name"), value = value, onChange = { setValue((it.target as HTMLInputElement).value) }) {
		attrs.onBlur = { submitCommand(props, oldValue, value) }
	}
}

fun RBuilder.jmTextField(handler: PropertyProps<String>.() -> Unit) = child(jmTextField) {
	attrs {
		handler()
	}
}
