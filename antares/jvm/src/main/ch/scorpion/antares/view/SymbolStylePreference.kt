package ch.scorpion.antares.view

import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class SymbolStylePreference : AbstractPreference(
	id = SymbolStyle.PROP_SYMBOL_STYLE,
	nameKey = "antares.action.symbolStyle"
) {

	private val editor = JComboBox<SymbolStyle>()
	private val value: SymbolStyle get() = SymbolStyle.withName(panel!!.preferences.getString(id))

	init {
		editor.addItem(SymbolStyle.AMERICAN)
		editor.addItem(SymbolStyle.EUROPEAN)

		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as SymbolStyle).customName)
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