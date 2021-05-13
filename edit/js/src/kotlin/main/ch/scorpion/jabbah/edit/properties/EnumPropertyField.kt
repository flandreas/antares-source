package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.ui.EnumProperty
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
	enumValues: Array<E>
): FunctionalComponent<PropertyProps<E>> {
	return functionalComponent(displayName) { props ->
		val oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
		var (value, setValue) = useState(oldValue)

		mSelect(value?.customName, onChange = { e, _ ->
			val newValue = enumValues.first { it.customName == e.targetValue }
			setValue(newValue)
			submitCommand(props, oldValue, newValue)
		}) {
			enumValues.forEach {
				mMenuItem(it.toString(), value = it.customName)
			}
		}
	}
}
