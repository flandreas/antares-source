package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.swing.DataFormPanel

/** Displays editors for all [Preference]s of a particular [PreferenceGroup].*/
class PreferencesPanel(
	private val group: PreferenceGroup,
	val preferences: Preferences,
	val messageDisplay: PreferencesMessageDisplay
) : DataFormPanel() {

	init {
		buildUI()
	}

	private fun buildUI() {
		for (preference in group.preferences) {
			preference.addToPanel(this)
		}

		addFiller()
	}

	fun load() {
		for (preference in group.preferences) {
			preference.load()
		}
	}
}