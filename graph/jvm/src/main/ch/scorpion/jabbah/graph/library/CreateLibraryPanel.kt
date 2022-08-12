package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.edit.properties.TranslatablePropertyEditor
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

data class CreateLibraryInfo(
	val name: TranslatableText,
	val description: TranslatableText?,
	val importUuid: UUID?
)

/**
 * A [JPanel] for collecting the input used for creating a new [Library].
 */
class CreateLibraryPanel(
	service: LibraryManagementService = LibraryModule.libraryManagementService
) : JPanel() {

	companion object {

		/** Makes sure the `null` entry gets rendered with a special text denoting the "empty library".*/
		class LibraryNameRenderer : DefaultListCellRenderer() {
			override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
				val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
				if (value == null) {
					renderer.text = Translations.getString("library.dialog.new.emptyTemplate.name")
				}
				return renderer
			}
		}

		fun showAsDialog(
			parent: Component = Frame.getFrames()[0],
			service: LibraryManagementService
		): CreateLibraryInfo? {
			val panel = CreateLibraryPanel(service)
			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					Translations.getString("library.dialog.new.name.dialog.title"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE)
				) {
				JOptionPane.OK_OPTION -> CreateLibraryInfo(
					panel.nameField.value as TranslatableText,
					panel.descriptionField.value as TranslatableText,
					panel.selectedImport?.uuid)
				else -> null
			}
		}
	}

	private val nameLabel = Translations.getString("library.property.name.name")

	private val nameField = TranslatablePropertyEditor(nameLabel)

	private val descriptionLabel = Translations.getString("library.property.desc.name")

	private val descriptionField = TranslatablePropertyEditor(descriptionLabel, multiline = { true } )

	private val importComboBox = JComboBox<LibraryDictionaryEntry>()

	private val selectedImport: LibraryDictionaryEntry? get() = importComboBox.selectedItem as LibraryDictionaryEntry?

	init {
		setupImportComboBox(service)
		buildEGBLLayout()

		(nameField.textComponent as JComponent).addAncestorListener(object : AncestorListener {
			override fun ancestorAdded(event: AncestorEvent?) {
				SwingUtilities.invokeLater {
					event?.let {
						it.component.requestFocus()
						it.component.removeAncestorListener(this)
					}
				}
			}

			override fun ancestorMoved(event: AncestorEvent?) {}
			override fun ancestorRemoved(event: AncestorEvent?) {}
		})
	}

	private fun setupImportComboBox(service: LibraryManagementService) {
		importComboBox.addItem(null)
		service.getLibraryDirectoryEntries().forEach { importComboBox.addItem(it) }
		importComboBox.renderer = LibraryNameRenderer()
	}

	private fun buildEGBLLayout() {
		val width = 250
		val inset = 5
		val rowDist = 5
		var row = 0
		layout = EGBL.getLayout()

		// Name

		EGBL.add(
			this,
			JLabel("$nameLabel:"),
			0, row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			0, inset, 0, 0
		)

		nameField.customEditor.preferredSize = Dimension(width, nameField.customEditor.preferredSize.height)
		nameField.customEditor.minimumSize = Dimension(width, nameField.customEditor.preferredSize.height)
		EGBL.add(
			this,
			nameField.customEditor,
			1, row,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, 10, 0, inset
		)

		// Description

		EGBL.add(
			this,
			JLabel("$descriptionLabel:"),
			0, ++row,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, inset, 0, 0
		)
		descriptionField.customEditor.preferredSize = Dimension(width, descriptionField.customEditor.preferredSize.height)
		descriptionField.customEditor.minimumSize = Dimension(width, descriptionField.customEditor.preferredSize.height)
		EGBL.add(
			this,
			descriptionField.customEditor,
			1, row,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, 10, 0, inset
		)

		// Imported library

		EGBL.add(
			this,
			JLabel(Translations.getString("library.dialog.new.import.label") + ":"),
			0, ++row,
			1, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			rowDist, inset, 0, 0
		)

		importComboBox.preferredSize = Dimension(width, importComboBox.preferredSize.height)
		EGBL.add(
			this,
			importComboBox,
			1, row,    // x, y
			EGBL.REMAINDER, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			rowDist, 10, 0, inset
		)

		val filler = JPanel()
		EGBL.add(
			this,
			filler,
			10, ++row,
			EGBL.REMAINDER, EGBL.REMAINDER,
			1.0, 1.0,
			EGBL.NORTHWEST,
			EGBL.BOTH
		)
	}
}