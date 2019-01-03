package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.preferences.AbstractPreference
import ch.scorpion.jabbah.base.preferences.PreferencesPanel
import ch.scorpion.jabbah.edit.snap.DottedGridPainter
import ch.scorpion.jabbah.edit.snap.LineGridPainter
import javax.swing.JComboBox

class GridPainterPreference : AbstractPreference(
	id = Grid.PROP_GRID_PAINTER,
	nameKey = "edit.preferences.Grid.painter"
) {

	private val editor = JComboBox<Type>()

	private val value: Type get() = Type.withId(panel!!.preferences.getString(id))

	init {
		editor.addItem(Type.Line)
		editor.addItem(Type.Dot)
		editor.addActionListener {
			if (panel != null) {
				panel?.preferences?.customize(id, (editor.selectedItem as Type).id)
			}
		}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}

	private enum class Type(val id: String, val nameKey: String) {
		Line(LineGridPainter.NAME, "edit.preferences.Grid.painter.line"),
		Dot(DottedGridPainter.NAME,"edit.preferences.Grid.painter.dot");

		companion object {
			fun withId(id: String): Type {
				return Type.values().first { it.id == id }
			}
		}

		override fun toString(): String {
			return Translations.getString(nameKey)
		}
	}
}