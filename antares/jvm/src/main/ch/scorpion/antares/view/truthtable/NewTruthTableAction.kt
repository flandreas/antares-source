package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.model.truthtable.OpenTruthTableItemRequest
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryFolderAction
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

/**
 * Asks the user for parameters of a new [TruthTable] and creates it as new [TruthTableLibraryItem]
 * in the currently selected [LibraryDirectory]
 */
class NewTruthTableAction(
	controller: LibraryTreeViewController,
) : AbstractLibraryFolderAction(
	actionBaseName = "library.action.newTruthTable",
	operation = Operation.Change,
	controller
) {

	private val operationTarget: Any? get() = if (selectedItem is LibraryDirectory) selectedFolder.library else null

	init {
		updateEnabledness()
	}

	override fun execute(event: ActionEvent) {
		NewTruthTablePanel
			.showAsDialog(Frame.getFrames()[0])
			?.let {
				val directory = controller.selectedItem as LibraryDirectory
				val library = directory.library!!
				val truthTableItem = TruthTableLibraryItem(it)

				library.libraryService.addLibraryItem(library, truthTableItem, directory)
				eventBus.post(OpenTruthTableItemRequest(truthTableItem))
			}
	}

	override val operationAuthorized: Boolean
		get() = operationTarget != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget!!)
}