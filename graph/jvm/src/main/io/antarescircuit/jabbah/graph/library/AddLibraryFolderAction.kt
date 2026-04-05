package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.ui.NewNamePanel
import io.antarescircuit.jabbah.edit.auth.Operation.Change
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

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
		updateEnabled()
	}

	override fun execute(event: ActionEvent) {
		NewNamePanel.showAsDialog(name)?.let {
			val directory = controller.selectedItem as LibraryDirectory
			directory.library!!.libraryService.addFolder(directory.library!!, TranslatableText(it), directory)
		}
	}
}