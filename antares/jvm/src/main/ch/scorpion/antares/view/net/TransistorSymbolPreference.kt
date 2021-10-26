package ch.scorpion.antares.view.net

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class TransistorSymbolPreference : AbstractPreference(
	id = TransistorViewSymbol.PROP_TRANSISTOR_SYMBOL,
	nameKey = "antares.preference.transistorSymbol.name"
) {
	private val editor = JComboBox<TransistorViewSymbol>()
	private val value: TransistorViewSymbol get() = TransistorViewSymbol.withName(panel!!.preferences.getString(id))

	init {
		editor.addItem(TransistorViewSymbol.Bulk)
		editor.addItem(TransistorViewSymbol.InverterCircle)

		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as TransistorViewSymbol).customName)
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