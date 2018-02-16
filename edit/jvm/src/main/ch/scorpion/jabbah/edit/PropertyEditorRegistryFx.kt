package ch.scorpion.jabbah.edit

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import org.controlsfx.control.PropertySheet
import org.controlsfx.property.editor.DefaultPropertyEditorFactory
import org.controlsfx.property.editor.PropertyEditor

/** Extends [DefaultPropertyEditorFactory] with the possibility to register value type to [PropertyEditor] mappings.*/
class PropertyEditorRegistryFx : DefaultPropertyEditorFactory() {

	companion object {
		fun installAutoSelectAll(control: TextInputControl) {
			control.focusedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
				if (newValue) {
					Platform.runLater { control.selectAll() }
				}
			}
		}
	}

	private val typeToEditor = mutableMapOf<Class<*>, Class<out PropertyEditor<*>>>()

	/** ---- [DefaultPropertyEditorFactory] */

	override fun call(item: PropertySheet.Item): PropertyEditor<*>? {

		if (item.type == String::class.java) {
			return createTextEditor(item)
		}

		val editor = super.call(item)
		if (editor != null) {
			return editor
		}

		val editorType = typeToEditor[item.type as Class<*>]
		if (editorType != null) {
			return createEditor(item, editorType)
		}

		return null
	}

	/** ---- [PropertyEditorRegistryFx] */

	fun register(valueType: Class<*>, editorType: Class<out PropertyEditor<*>>) {
		typeToEditor[valueType] = editorType
	}

	private fun createEditor(item: PropertySheet.Item, editorType: Class<out PropertyEditor<*>>): PropertyEditor<*> {
		return editorType.getConstructor(PropertySheet.Item::class.java).newInstance(item)
	}

	private fun createTextEditor(property: PropertySheet.Item): PropertyEditor<*> {
		return object : AbstractFocusPropertyEditor<String,TextField>(property, TextField()) {

			init {
				installAutoSelectAll(control)
			}

			override fun setValue(value: String?) {
				return control.setText(value)
			}

			override fun getObservableValue(): ObservableValue<String> {
				return control.textProperty()
			}

		}
	}
}