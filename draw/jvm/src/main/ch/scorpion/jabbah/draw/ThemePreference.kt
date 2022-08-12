package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.jabbah.draw.style.Themes
import javax.swing.JComboBox

class ThemePreference : AbstractPreference(
	id = Themes.PROP_THEME,
	nameKey = "draw.preference.theme.name",
	needsRestart = true
) {

	private val editor = JComboBox<Theme>()

	private val value: Theme get() = Themes.get(panel!!.preferences.getString(id))!!

	init {
		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as Theme).name)
		}
		registerEditor(editor)
	}

	override fun addToPanel(panel: PreferencesPanel) {
		if (editor.itemCount == 0) {
			Themes.allThemes().forEach { editor.addItem(it) }
		}

		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}
}