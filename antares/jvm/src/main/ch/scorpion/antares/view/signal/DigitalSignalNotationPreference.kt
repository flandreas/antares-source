package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.DigitalSignalNotation
import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class DigitalSignalNotationPreference : AbstractPreference(
	id = DigitalSignalNotation.PROP_DIGITAL_SIGNAL_NOTATION,
	nameKey = "antares.preferences.DigitalSignalNotation"
) {

	private val editor = JComboBox<DigitalSignalNotation>()
	private val value: DigitalSignalNotation get() = DigitalSignalNotation.withName(panel!!.preferences.getString(id))

	init {
		editor.addItem(DigitalSignalNotation.PREFIX)
		editor.addItem(DigitalSignalNotation.BASE_SUBSCRIPT)
		editor.addItem(DigitalSignalNotation.SUFFIX)

		editor.addActionListener {
			panel?.preferences?.customize(id, (editor.selectedItem as DigitalSignalNotation).customName)
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