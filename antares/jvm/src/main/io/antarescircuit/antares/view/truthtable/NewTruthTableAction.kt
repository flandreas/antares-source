package io.antarescircuit.antares.view.truthtable

import io.antarescircuit.antares.model.truthtable.OpenTruthTableItemRequest
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractLibraryDirectoryAction
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

/**
 * Asks the user for parameters of a new [TruthTable] and creates it as new [TruthTableLibraryItem]
 * in the currently selected [LibraryDirectory].
 */
class NewTruthTableAction(
	controller: LibraryTreeViewController,
) : AbstractLibraryDirectoryAction(
	actionBaseName = "library.action.newTruthTable",
	operation = Operation.Change,
	controller
) {

	init {
		updateEnabled()
	}

	override fun execute(event: ActionEvent) {
		NewTruthTablePanel
			.showAsDialog(name, Frame.getFrames()[0])
			?.let {
				val directory = controller.selectedItem as LibraryDirectory
				val library = directory.library!!
				val truthTableItem = TruthTableLibraryItem(it)

				library.libraryService.addLibraryItem(library, truthTableItem, directory)
				controller.eventBus.post(OpenTruthTableItemRequest(truthTableItem))
			}
	}
}