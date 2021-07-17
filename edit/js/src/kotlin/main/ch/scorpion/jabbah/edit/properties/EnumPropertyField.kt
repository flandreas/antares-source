package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.EnumProperty
import com.ccfraser.muirwik.components.mSelect
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ccfraser.muirwik.components.targetValue
import react.FunctionalComponent
import react.functionalComponent
import react.useState

/**
 * Creates a React material UI select edit field for a [EnumProperty].
 */
fun <E : EnumProperty<E>> enumPropertyField(
	displayName: String? = null,
	enumValues: Array<E>,
	cmdFactory: (PropertyProps<E>, newValue: E?) -> PropertyCommandJs<E> = { props, newValue -> propertyCommandFactory(props, newValue) }
): FunctionalComponent<PropertyProps<E>> {
	return functionalComponent(displayName) { props ->
		var oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
		val (value, setValue) = useState(oldValue)

		mSelect(value?.customName, onChange = { e, _ ->
			val newValue = enumValues
				.filter { props.filter?.invoke(it) ?: true }
				.first { it.customName == e.targetValue }
			setValue(newValue)
			submitCommand(props, newValue, cmdFactory)
			oldValue = newValue
		}) {
			enumValues
				.filter { props.filter?.invoke(it) ?: true }
				.forEach {
				mMenuItem(it.toString(), value = it.customName)
			}
		}
	}
}
