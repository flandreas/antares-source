package ch.scorpion.jabbah.edit

import javafx.beans.value.ObservableValue
import javafx.scene.Node
import org.controlsfx.property.editor.PropertyEditor
import org.controlsfx.control.PropertySheet
/**
 * Abstract base class of [PropertyEditor] implementations to be used in [PropertySheet]s.
 *
 * Unlike the default editors created by JavaFX, which use binding and therefore forward every update in the
 * editor to the bound property (such as every key stroke in a text field), this class defers updates until
 * the control looses focus. This is used to avoid creating too many [Command]s.
 *
 * @param T the type of the property value being edited
 * @param C the type of the [Node] used to edit the property
 */
abstract class AbstractFocusPropertyEditor<T, out C : Node>(
	protected val property: PropertySheet.Item,
	protected val control: C,
	readonly: Boolean = !property.isEditable
) : PropertyEditor<T> {

	private var suspendUpdate: Boolean = false

	init {
		if (!readonly) {
			installFocusListener()
			installPropertyListener()
		}
	}

	/** ----- [PropertyEditor] */

	override fun getValue(): T = getObservableValue().value

	override fun getEditor(): Node = control

	/** ---- [AbstractFocusPropertyEditor] */

	protected abstract fun getObservableValue(): ObservableValue<T>

	private fun installPropertyListener() {
		if (property.observableValue.isPresent) {
			property.observableValue.get().addListener { _: ObservableValue<out Any>, _: Any, _: Any ->
				if (!suspendUpdate) {
					suspendUpdate = true
					this@AbstractFocusPropertyEditor.value = property.value as T
					suspendUpdate = false
				}
			}
		}
	}

	private fun installFocusListener() {
		control.focusedProperty().addListener { _, oldValue, newValue ->
			if (oldValue && !newValue) {
				suspendUpdate = true
				this@AbstractFocusPropertyEditor.property.value = value
				suspendUpdate = false
			}
		}
	}
}