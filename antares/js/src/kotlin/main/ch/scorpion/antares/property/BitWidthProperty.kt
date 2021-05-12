package ch.scorpion.antares.property

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.properties.PropertyProps
import ch.scorpion.jabbah.edit.properties.submitCommand
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.mSelect
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ccfraser.muirwik.components.targetValue
import react.RBuilder
import react.child
import react.functionalComponent
import react.useState

val jmBitWidthField = functionalComponent<PropertyProps<BitWidth>>("BitWidth") { props ->
	val oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
	var (value, setValue) = useState(oldValue)
	mFormControl {
		mSelect(value, onChange = { e, _ ->
			setValue(BitWidth.withName(e.targetValue as String))
		}) {
			attrs.onBlur = { submitCommand(props, oldValue, value) }
			BitWidth.values().forEach { bitWidth ->
				mMenuItem(bitWidth.toString(), value = bitWidth.toString())
			}
		}
	}
}

fun RBuilder.jmBitWidthField(handler: PropertyProps<BitWidth>.() -> Unit) = child(jmBitWidthField) {
	attrs {
		handler()
	}
}