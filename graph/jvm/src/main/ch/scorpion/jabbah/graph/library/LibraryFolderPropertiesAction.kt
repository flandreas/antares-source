package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.edit.TranslatableTextPropertyEditor
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import javax.swing.Action
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

/** An [Action] for editing the properties of a [LibraryFolder], which is currently only its translatable name.*/
class LibraryFolderPropertiesAction(
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.editFolderProperties", libraryTreeView, eventBus) {

	override fun execute(event: ActionEvent) {
		val title = Translations.getString("library.action.editFolderProperties.name")
		var text: TranslatableText?
		while (true) {
			text = LibraryFolderPropertiesPanel.showAsDialog(
				title = title,
				name = selectedFolder.name.translation)
			if (text == null) {
				return
			}
			if (!text.hasDefaultOrSystemLanguage()) {
				if (JOptionPane.showConfirmDialog(
						Frame.getFrames()[0],
						Translations.getString("base.translation.incomplete.msg"),
						title,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION
				) {
					return
				}
			} else {
				break
			}
		}
		selectedFolder.library!!.libraryService.renameDirectory(selectedFolder, text!!)
	}
}

private class LibraryFolderPropertiesPanel(
	name: TranslatableText
) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Component = Frame.getFrames()[0],
			title: String,
			name: TranslatableText
		): TranslatableText? {
			val panel = LibraryFolderPropertiesPanel(name)
			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE)
				) {
				JOptionPane.OK_OPTION -> panel.nameField.value as TranslatableText
				else -> null
			}
		}
	}

	private val nameLabel = Translations.getString("library.property.name.name")
	private val nameField = TranslatableTextPropertyEditor(nameLabel)

	init {
		preferredSize = Dimension(300, 100)
		buildUI()
		nameField.value = name
	}

	private fun buildUI() {
		val inset = 5
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