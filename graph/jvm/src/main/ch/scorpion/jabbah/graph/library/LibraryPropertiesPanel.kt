package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.properties.TranslatablePropertyEditor
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.LibraryVisibilityEditor
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

/** A [JPanel] for editing the properties of a [Library].*/
class LibraryPropertiesPanel(
	properties: LibraryProperties? = null,
	editable: Boolean = true
) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Component = Frame.getFrames()[0],
			title: String,
			properties: LibraryProperties? = null,
			editable: Boolean = true
		): LibraryProperties? {
			val panel = LibraryPropertiesPanel(properties, editable)
			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE)
			) {
				JOptionPane.OK_OPTION -> LibraryProperties(
					panel.nameField.value as TranslatableText,
					panel.descField.value as TranslatableText,
					panel.visibilityField.value as LibraryVisibility)
				else -> null
			}
		}
	}

	private val nameLabel = Translations.getString("library.property.name.name")
	private val descLabel = Translations.getString("library.property.desc.name")
	private val nameField = TranslatablePropertyEditor(nameLabel, editable = editable)
	private val descField = TranslatablePropertyEditor(descLabel, multiline = { true }, rows = 8, editable = editable)
	private val visibilityLabel = Translations.getString("library.property.visibility.name")
	private val visibilityField = LibraryVisibilityEditor()

	init {
		preferredSize = Dimension(400, 180)
		visibilityField.customEditor.isEnabled = editable

		buildUI()

		properties?.let {
			nameField.value = it.name
			descField.value = it.description
			visibilityField.value = it.visibility
		}
	}

	private fun buildUI() {
		val inset = 5
		val rowDist = 5
		layout = EGBL.getLayout()

		EGBL.add(
			this,
			JLabel("$nameLabel:"),
			0, 0,
			1, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, inset, 0, 0
		)

		EGBL.add(
			this,
			nameField.customEditor,
			1, 0,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			0, inset, 0, 0
		)

		EGBL.add(
			this,
			JLabel("$descLabel:"),
			0, 1,
			1, 1,
			0.0, 0.0,
			EGBL.NORTHWEST,
			EGBL.NONE,
			4 + rowDist, inset, 0, 0
		)

		EGBL.add(
			this,
			descField.customEditor,
			1, 1,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.HORIZONTAL,
			rowDist, inset, 0, 0
		)

		if (GraphModuleJvm.supportWeb) {
			EGBL.add(
				this,
				JLabel("$visibilityLabel:"),
				0, 2,
				1, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				rowDist, inset, 0, 0
			)

			EGBL.add(
				this,
				visibilityField.customEditor,
				1, 2,
				EGBL.REMAINDER, 1,
				0.0, 0.0,
				EGBL.WEST,
				EGBL.NONE,
				rowDist, inset, 0, 0
			)
		}

		val filler = JPanel()
		EGBL.add(
			this,
			filler,
			10, 10,
			EGBL.REMAINDER, EGBL.REMAINDER,
			1.0, 1.0,
			EGBL.NORTHWEST,
			EGBL.BOTH
		)
	}
}