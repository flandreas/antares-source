package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class BooleanExpressionNotationPreference : AbstractPreference(
	id = BooleanExpressionNotation.PROP_NOTATION,
	nameKey = "antares.preference.expression.notation"
) {
	private val editor = JComboBox<BooleanExpressionNotation>()
	private val value: BooleanExpressionNotation get() = BooleanExpressionNotation.withName(panel!!.preferences.getString(id))

	init {
		BooleanExpressionNotation.values().forEach {
			editor.addItem(it)
		}

		editor.addActionListener {
			panel?.preferences?.customize(this, (editor.selectedItem as BooleanExpressionNotation).customName)
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