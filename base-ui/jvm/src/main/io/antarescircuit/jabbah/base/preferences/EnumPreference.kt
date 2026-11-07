package io.antarescircuit.jabbah.base.preferences

import io.antarescircuit.jabbah.base.EnumProperty
import javax.swing.JComboBox

class EnumPreference<T : EnumProperty<*>>(
	id: String,
	nameKey: String,
	values: Array<T>,
	private val withName: (String) -> T,
	needsRestart: Boolean = false
) : AbstractPreference(id, nameKey, needsRestart) {

	private val editor = JComboBox<EnumProperty<*>>()

	override var editable: Boolean = true
		set(value) {
			field = value
			editor.isEnabled = value
		}

	init {
		values.forEach { editor.addItem(it) }
		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.getItemAt(editor.selectedIndex)).customName)
		}
		registerEditor(editor)
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = withName(panel!!.preferences.getString(id))
	}
}