package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * Asks the user for the name of the new [LibraryDirectory] and adds a new [LibraryDirectory] as a child of
 * the currently selected [LibraryDirectory].
 */
class AddLibraryFolderAction(
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.addFolder", true, libraryTreeView, eventBus) {

	override fun execute(event: ActionEvent) {
		val name = JOptionPane.showInputDialog(
			SwingUtilities.getWindowAncestor(libraryTreeView),
			Translations.getString("library.action.addFolder.question"),
			name,
			JOptionPane.QUESTION_MESSAGE
		)

		if (StringUtils.isEmpty(name)) {
			return
		}

		val directory = libraryTreeView.getSelectedItem() as LibraryDirectory
		directory.library!!.libraryService.addFolder(directory.library!!, TranslatableText(name), directory)
	}

	override fun calculateEnabledness(): Boolean {
		return super.calculateEnabledness() && isLibraryOwnedByUser
	}
}