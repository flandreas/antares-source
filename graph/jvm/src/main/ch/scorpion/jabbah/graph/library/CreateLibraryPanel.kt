package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import java.awt.Component
import java.awt.Frame
import javax.swing.*
import javax.swing.JPanel
import java.awt.Dimension
import javax.swing.JTextField
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener


/**
 * A [JPanel] for collecting the input used for creating a new [Library].
 */
class CreateLibraryPanel(
	service: LibraryManagementService = LibraryModule.libraryManagementService
) : JPanel() {

	data class CreateLibraryInfo(val libraryName: String, val templateUuid: UUID?)

	companion object {

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
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
				JOptionPane.OK_OPTION -> CreateLibraryInfo(panel.nameField.text, panel.selectedTemplate!!.uuid)
				else -> null
			}
		}
	}

	private val nameField = JTextField()

	private val templateComboBox = JComboBox<LibraryDictionaryEntry>()

	private val selectedTemplate: LibraryDictionaryEntry? get() = templateComboBox.selectedItem as LibraryDictionaryEntry?

	init {
		setupTemplateComboBox(service)
		buildEGBLLayout()

		nameField.addAncestorListener(object : AncestorListener {
			override fun ancestorAdded(event: AncestorEvent?) {
				nameField.requestFocusInWindow()
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
			JLabel(Translations.getString("library.property.name.name") + ":"),
			0, 0,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			0, inset, 0, 0
		)

		nameField.preferredSize = Dimension(200, nameField.preferredSize.height)
		nameField.minimumSize = Dimension(200, nameField.preferredSize.height)
		EGBL.add(
			this,
			nameField,
			1, 0,    // x, y
			EGBL.REMAINDER, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
			0, 10, 0, inset
		)

		EGBL.add(
			this,
			JLabel(Translations.getString("library.dialog.new.template.label") + ":"),
			0, 1,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.WEST,    // anchor
			EGBL.NONE,    // fill
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
			10, 10, // x, y
			EGBL.REMAINDER, EGBL.REMAINDER, // width, height
			1.0, 1.0, // weightX, weightY
			EGBL.NORTHWEST, // anchor
			EGBL.BOTH    // fill
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