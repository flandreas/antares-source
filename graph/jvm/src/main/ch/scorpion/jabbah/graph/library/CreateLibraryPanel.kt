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


/**
 * A [JPanel] for collecting the input used for creating a new [Library].
 */
class CreateLibraryPanel(
	service: LibraryManagementService = LibraryModule.libraryManagementService
) : JPanel() {

	data class CreateLibraryInfo(val name: TranslatableText, val templateUuid: UUID?)

	companion object {

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
				JOptionPane.OK_OPTION -> CreateLibraryInfo(panel.nameField.value as TranslatableText, panel.selectedTemplate?.uuid)
				else -> null
			}
		}
	}

	private val nameLabel = Translations.getString("library.property.name.name")

	private val nameField = TranslatablePropertyEditor(nameLabel)

	private val templateComboBox = JComboBox<LibraryDictionaryEntry>()

	private val selectedTemplate: LibraryDictionaryEntry? get() = templateComboBox.selectedItem as LibraryDictionaryEntry?

	init {
		setupTemplateComboBox(service)
		buildEGBLLayout()

		(nameField.customEditor as JComponent).addAncestorListener(object : AncestorListener {
			override fun ancestorAdded(event: AncestorEvent?) {
				nameField.customEditor.requestFocusInWindow()
			}

			override fun ancestorMoved(event: AncestorEvent?) {}
			override fun ancestorRemoved(event: AncestorEvent?) {}
		})
	}

	private fun setupTemplateComboBox(service: LibraryManagementService) {
		templateComboBox.addItem(null)
		service.getLibraryDirectoryEntries().forEach { templateComboBox.addItem(it) }
		templateComboBox.renderer = LibraryNameRenderer()
	}

	private fun buildEGBLLayout() {
		val inset = 5
		layout = EGBL.getLayout()

		EGBL.add(
			this,
			JLabel("$nameLabel:"),
			0, 0,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			0, inset, 0, 0
		)

		nameField.customEditor.preferredSize = Dimension(200, nameField.customEditor.preferredSize.height)
		nameField.customEditor.minimumSize = Dimension(200, nameField.customEditor.preferredSize.height)
		EGBL.add(
			this,
			nameField.customEditor,
			1, 0,
			EGBL.REMAINDER, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, 10, 0, inset
		)

		EGBL.add(
			this,
			JLabel(Translations.getString("library.dialog.new.template.label") + ":"),
			0, 1,
			1, 1,
			0.0, 0.0,
			EGBL.WEST,
			EGBL.NONE,
			0, inset, 0, 0
		)

		templateComboBox.preferredSize = Dimension(200, templateComboBox.preferredSize.height)
		EGBL.add(
			this,
			templateComboBox,
			1, 1,    // x, y
			EGBL.REMAINDER, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			0, 10, 0, inset
		)

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

	/** Makes sure the `null` entry gets rendered with a special text denoting the "empty library".*/
	private class LibraryNameRenderer : DefaultListCellRenderer() {
		override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
			if (value == null) {
				renderer.text = Translations.getString("library.dialog.new.emptyTemplate.name")
			}
			return renderer
		}
	}
}