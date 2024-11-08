package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.swing.DataFormPanel

/** Displays editors for all [Preference]s of a particular [PreferenceGroup].*/
class PreferencesPanel(
	private val displayedPreferences: () -> Iterator<Preference>,
	val preferences: Preferences,
	val messageDisplay: PreferencesMessageDisplay,
	addFiller: Boolean = true
) : DataFormPanel() {

	constructor(
		group: PreferenceGroup,
		preferences: Preferences,
		messageDisplay: PreferencesMessageDisplay
	): this({ group.preferences}, preferences, messageDisplay)

	init {
		buildUI(addFiller)
	}

	private fun buildUI(addFiller: Boolean) {
		for (preference in displayedPreferences()) {
			preference.addToPanel(this)
		}

		if (addFiller) {
			addFiller()
		}
	}

	fun load() {
		for (preference in displayedPreferences()) {
			preference.load()
		}
	}
}