package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/** An [Action] for deleting the currently selected [LibraryDirectory].*/
class DeleteLibraryFolderAction(
	controller: LibraryTreeViewController,
	private val operationTarget: () -> Any?
) : AbstractLibraryFolderAction(
	actionBaseName = "graph.action.deleteLibraryDirectory",
	operation = Change,
	controller
) {

	companion object {
		private val LOG by logger(DeleteLibraryItemAction::class)
	}

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)

	override fun calculateEnabledness(): Boolean {
		return super.calculateEnabledness() && (selectedItem as LibraryDirectory).isEmpty()
	}

	override fun execute(event: ActionEvent) {
		val libraryItem = controller.selectedItem
		if (JOptionPane.showConfirmDialog(
				SwingUtilities.getWindowAncestor(controller.view as Component),
				Translations.getString("graph.action.deleteLibraryDirectory.question", controller.selectedItem!!),
				name,
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			val library = libraryItem!!.library!!
			LOG.userTrail("Delete folder '${libraryItem.name.getOptionalTranslation()}'")
			library.libraryService.removeLibraryItem(libraryItem.library!!, libraryItem, folderOfSelectedItem as LibraryDirectory)
		}
	}
}