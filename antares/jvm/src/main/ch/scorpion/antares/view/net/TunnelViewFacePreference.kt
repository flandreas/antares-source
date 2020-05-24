package ch.scorpion.antares.view.net

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class TunnelViewFacePreference : AbstractPreference(
	id = TunnelViewFace.PROP_TUNNEL_FACE,
	nameKey = "antares.preference.TunnelViewFace"
) {

	private val editor = JComboBox<TunnelViewFace>()
	private val value: TunnelViewFace get() = TunnelViewFace.withName(panel!!.preferences.getString(id))

	init {
		editor.addItem(TunnelViewFace.TUNNEL)
		editor.addItem(TunnelViewFace.ARROW)

		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as TunnelViewFace).customName)
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