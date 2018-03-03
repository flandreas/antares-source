package ch.scorpion.jabbah.edit

import org.controlsfx.control.PropertySheet
import org.controlsfx.property.editor.AbstractPropertyEditor
import ch.scorpion.jabbah.base.fx.NumericField
import javafx.application.Platform
import javafx.beans.value.ObservableValue

/**
 * A [PropertyEditor] for editing optional [Number]s.
 */
open class OptionalNumericEditor(
	property: PropertySheet.Item
) : AbstractFocusPropertyEditor<Number, NumericField>(property, NumericField(property.type as Class<out Number>) ) {

	private var sourceClass: Class<out Number> = property.type as Class<out Number>

	init {
		editor.focusedProperty().addListener {
			_ -> Platform.runLater { control.selectAll() }
		}
	}

	override fun getObservableValue(): ObservableValue<Number> {
		return control.valueProperty()
	}

	override fun setValue(value: Number?) {
		if (value == null) {
			control.text = ""
		} else {
			sourceClass = value.javaClass
			control.text = value.toString()
		}
	}
}