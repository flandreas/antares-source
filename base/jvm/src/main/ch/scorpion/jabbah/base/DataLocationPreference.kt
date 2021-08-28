package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class DataLocationPreference : AbstractPreference(
	id = DataLocation.PROP_DATA_LOCATION,
	nameKey = "base.preferences.dataLocation",
	needsRestart = true
) {

	private val editor = JComboBox<DataLocation>()

	private val value: DataLocation get() = DataLocation.withName(panel!!.preferences.getString(id))

	init {
		DataLocation.values().forEach { editor.addItem(it) }
		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as DataLocation).customName)
		}
		// Feature not yet released
		editor.isEnabled = false
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}
}