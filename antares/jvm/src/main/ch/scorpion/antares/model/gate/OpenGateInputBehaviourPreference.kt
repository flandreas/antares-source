package ch.scorpion.antares.model.gate

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class OpenGateInputBehaviourPreference : AbstractPreference(
	id = OpenGateInputBehavior.PROP_OPEN_GATE_INPUT_BEHAVIOR,
	nameKey = "antares.preference.openGateInputBehavior.name"
) {
	private val editor = JComboBox<OpenGateInputBehavior>()
	private val value: OpenGateInputBehavior get() = OpenGateInputBehavior.withName(panel!!.preferences.getString(id))

	init {
		editor.addItem(OpenGateInputBehavior.Accept)
		editor.addItem(OpenGateInputBehavior.Random)
		editor.addItem(OpenGateInputBehavior.Error)

		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as OpenGateInputBehavior).customName)
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