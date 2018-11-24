package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.Frame
import javax.swing.*

/** A [JPanel] for editing the properties of a [Library].*/
class LibraryPropertiesPanel(properties: LibraryProperties? = null) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			title: String,
			properties: LibraryProperties? = null
		): LibraryProperties? {
			val panel = LibraryPropertiesPanel(properties)
			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE)
			) {
				JOptionPane.OK_OPTION -> LibraryProperties(panel.nameField.text, panel.descField.text)
				else -> null
			}
		}
	}

	private val nameField = JTextField()

	private val descField = JTextArea()

	init {
		buildUI()
		properties?.let {
			nameField.text = it.name
			descField.text = it.description
		}
	}

	private fun buildUI() {
		val inset = 5
		layout = EGBL.getLayout()

		EGBL.add(
			this,
			JLabel(Translations.getString("library.property.name.name") + ":"),
			0, 0,	// x, y
			1, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			0, inset, 0, 0
		)

		EGBL.add(
			this,
			nameField,
			1, 0,	// x, y
			EGBL.REMAINDER, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.HORIZONTAL,	// fill
			0, inset, 0, 0
		)

		EGBL.add(
			this,
			JLabel(Translations.getString("library.property.desc.name") + ":"),
			0, 1,	// x, y
			1, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			0, inset, 0, 0
		)

		descField.rows = 4
		descField.columns = 20
		EGBL.add(
			this,
			UiUtil.decorateTextArea(descField),
			1, 1,	// x, y
			EGBL.REMAINDER, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.HORIZONTAL,	// fill
			0, inset, 0, 0
		)

		val filler = JPanel()
		EGBL.add(
			this,
			filler,
			10, 10, // x, y
			EGBL.REMAINDER, EGBL.REMAINDER, // width, height
			1.0, 1.0, // weightX, weightY
			EGBL.NORTHWEST, // anchor
			EGBL.BOTH    // fill
		)
	}
}