package io.antarescircuit.jabbah.draw

import io.antarescircuit.jabbah.base.preferences.AbstractPreference
import io.antarescircuit.jabbah.base.preferences.PreferencesPanel
import io.antarescircuit.jabbah.draw.style.Theme
import io.antarescircuit.jabbah.draw.style.Themes
import javax.swing.JComboBox

class ThemePreference : AbstractPreference(
	id = Themes.PROP_THEME,
	nameKey = "draw.preference.theme.name",
	needsRestart = true
) {

	private val editor = JComboBox<Theme>()

	private val value: Theme get() = Themes.get(panel!!.preferences.getString(id))!!

	override var editable: Boolean = true
		set(value) {
			field = value
			editor.isEnabled = value
		}

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