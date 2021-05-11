package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyGetter
import ch.scorpion.jabbah.edit.PropertySetter
import com.ccfraser.muirwik.components.mTextField
import org.w3c.dom.HTMLInputElement
import react.*

external interface TextFieldProps : RProps {
	var editor: Editor
	var propertyBaseKey: String
	var beanProvider: BeanProvider
	var beanIds: List<Int>
	var getter: PropertyGetter<String>
	var setter: PropertySetter<String>
}

val jmTextField = functionalComponent<TextFieldProps>("TextField") { props ->
	val oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
	var (value, setValue) = useState(oldValue)
	mTextField(Translations.getString("edit.property.name"), defaultValue = oldValue, onChange = { setValue((it.target as HTMLInputElement).value) }) {
		attrs.onBlur = {
			println("onBlur: $value")
			val command = PropertyCommandJs(props.editor, props.propertyBaseKey, props.beanProvider, props.beanIds, value, props.getter, props.setter)
			command.establishOldValue()

			if (value != oldValue) {
				props.editor.commandManager.apply {
					try {
						beginTransaction(command)
						commitTransaction()
					} catch (t: Throwable) {
						if (isInTransaction) {
							rollbackTransaction()
						}
					}
				}
			}
		}
	}
}

fun RBuilder.jmTextField(handler: TextFieldProps.() -> Unit) = child(jmTextField) {
	attrs {
		handler()
	}
}
