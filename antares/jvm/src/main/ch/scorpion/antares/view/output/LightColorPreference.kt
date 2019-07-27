package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.LightColorEditor
import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox
import javax.swing.JComponent

class LightColorPreference : AbstractPreference(
	id = LightColor.PROP_DEFAULT_LIGHT_COLOR,
	nameKey = "antares.preferences.LightColor"
) {

	private val editor = LightColorEditor()

	private val value: LightColor get() = LightColor.withName(panel!!.preferences.getString(id))

	private val combobox: JComboBox<*> get() = editor.customEditor as JComboBox<*>

	init {
		(editor.customEditor as JComboBox<*>).addActionListener {
			if (panel != null && combobox.selectedItem != null) {
				panel?.preferences?.customize(id, (combobox.selectedItem as LightColor).customName)
			}
		}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, combobox)
	}

	override fun load() {
		combobox.selectedItem = value
	}
}