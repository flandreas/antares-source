package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class LanguagePreference : AbstractPreference(
	id = Language.PROP_LANGUAGE,
	nameKey = "base.preferences.language",
	needsRestart = true
) {

	private val editor = JComboBox<Language>()

	private val value: Language get() = Language.withCode(panel!!.preferences.getString(id))

	init {
		Language.values().forEach { editor.addItem(it) }
		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as Language).code)
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