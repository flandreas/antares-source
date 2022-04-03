package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.EnumProperty
import javax.swing.JComboBox

class EnumPreference<T : Enum<T>>(
	id: String,
	nameKey: String,
	values: Array<T>,
	private val withName: (String) -> T,
	needsRestart: Boolean = false
) : AbstractPreference(id, nameKey, needsRestart) {

	private val editor = JComboBox<EnumProperty<T>>()

	init {
		values.forEach { editor.addItem(it as EnumProperty<T>) }
		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as EnumProperty<T>).customName)
		}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = withName(panel!!.preferences.getString(id))
	}
}