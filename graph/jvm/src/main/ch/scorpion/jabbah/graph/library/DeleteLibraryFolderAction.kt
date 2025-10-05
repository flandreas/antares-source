package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/** An [Action] for deleting the currently selected [LibraryDirectory].*/
class DeleteLibraryFolderAction(
	controller: LibraryTreeViewController
) : AbstractLibraryDirectoryAction(
	actionBaseName = "graph.action.deleteLibraryDirectory",
	operation = Change,
	controller
) {

	companion object {
		private val LOG by logger(DeleteLibraryItemAction::class)
	}

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness() && (selectedItem as LibraryDirectory).isEmpty() && selectedItem !is Library

	override fun execute(event: ActionEvent) {
		val libraryItem = controller.selectedItem
		if (JOptionPane.showConfirmDialog(
				SwingUtilities.getWindowAncestor(controller.view as Component),
				Translations.getString("graph.action.deleteLibraryDirectory.question", controller.selectedItem!!),
				name,
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			val library = libraryItem!!.library!!
			LOG.userTrail("Delete folder '${libraryItem.name.getOptionalTranslation()}'")
			library.libraryService.removeLibraryItem(libraryItem.library!!, libraryItem)
		}
	}
}