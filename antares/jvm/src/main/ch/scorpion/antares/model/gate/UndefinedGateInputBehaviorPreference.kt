package ch.scorpion.antares.model.gate

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class UndefinedGateInputBehaviorPreference : AbstractPreference(
	id = UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR,
	nameKey = "antares.preference.undefinedGateInputBehavior.name"
) {
	private val editor = JComboBox<UndefinedGateInputBehavior>()
	private val value: UndefinedGateInputBehavior get() = UndefinedGateInputBehavior.withName(panel!!.preferences.getString(id))

	init {
		UndefinedGateInputBehavior.values().forEach { editor.addItem(it) }
		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as UndefinedGateInputBehavior).customName)
		}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}
}