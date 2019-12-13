package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.TranslatableTextPanel
import java.awt.Frame
import javax.swing.JOptionPane
import javax.swing.Action

/** An [Action] for editing the properties of a [LibraryFolder], which is currently only its translatable name.*/
class LibraryFolderPropertiesAction(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.editFolderProperties", eventBus) {

	override fun execute(event: ActionEvent) {
		val title = Translations.getString("library.action.editFolderProperties.name")
		var text: TranslatableText?
		while (true) {
			text = TranslatableTextPanel.showAsDialog(
				title = title,
				text = selectedFolder.name.translation,
				textFieldColumns = 10)
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