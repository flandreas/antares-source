package ch.scorpion.jabbah.edit

import org.controlsfx.control.PropertySheet
import org.controlsfx.property.editor.DefaultPropertyEditorFactory
import org.controlsfx.property.editor.PropertyEditor

/** Extends [DefaultPropertyEditorFactory] with the possibility to register value type to [PropertyEditor] mappings.*/
class PropertyEditorRegistryFx : DefaultPropertyEditorFactory() {

	private val typeToEditor = mutableMapOf<Class<*>, Class<out PropertyEditor<*>>>()

	/** ---- [DefaultPropertyEditorFactory] */

	override fun call(item: PropertySheet.Item): PropertyEditor<*>? {
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
}