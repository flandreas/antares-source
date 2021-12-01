package ch.scorpion.jabbah.edit.properties

import com.ccfraser.muirwik.components.mSelect
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ccfraser.muirwik.components.targetValue
import react.FunctionalComponent
import react.functionalComponent
import react.useState

fun <T> selectPropertyField(
	displayName: String? = null,
	values: List<T>,
	cmdFactory: (PropertyProps<T>, newValue: T?) -> PropertyCommandJs<T> = { props, newValue -> propertyCommandFactory(props, newValue) }
): FunctionalComponent<PropertyProps<T>> {
	return functionalComponent(displayName) { props ->
		var oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
		val (value, setValue) = useState(oldValue)

		mSelect(value?.toString(), onChange = { e, _ ->
			val newValue = values
				.filter { props.filter?.invoke(it) ?: true }
				.first { it.toString() == e.targetValue }
			setValue(newValue)
			submitCommand(props, newValue, cmdFactory)
			oldValue = newValue
		}) {
			values
				.filter { props.filter?.invoke(it) ?: true }
				.forEach {
					mMenuItem(it.toString(), value = it.toString())
				}
		}
	}
}