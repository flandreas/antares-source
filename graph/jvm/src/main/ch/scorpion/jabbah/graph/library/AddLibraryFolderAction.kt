package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.ui.NewNamePanel
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * Asks the user for the name of the new [LibraryDirectory] and adds a new [LibraryDirectory] as a child of
 * the currently selected [LibraryDirectory].
 */
class AddLibraryFolderAction(
	controller: LibraryTreeViewController
) : AbstractLibraryDirectoryAction(
	actionBaseName = "library.action.addFolder",
	operation = Change,
	controller
) {
	init {
		updateEnabledness()
	}

	override fun execute(event: ActionEvent) {
		NewNamePanel.showAsDialog(name)?.let {
			val directory = controller.selectedItem as LibraryDirectory
			directory.library!!.libraryService.addFolder(directory.library!!, TranslatableText(it), directory)
		}
	}
}