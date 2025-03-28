package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.FSMLibraryItem
import ch.scorpion.antares.model.fsm.OpenFSMLibraryItemRequest
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.ui.NewNamePanel
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.AbstractLibraryFolderAction
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

class NewFSMAction(
    controller: LibraryTreeViewController
) : AbstractLibraryFolderAction(
    actionBaseName = "library.action.newFSM",
    operation = Operation.Change,
    controller
) {

    private val operationTarget: Any? get() = if (selectedItem is LibraryDirectory) selectedFolder.library else null

    override val opensDialog: Boolean get() = true

    init {
        updateEnabledness()
    }

    override fun execute(event: ActionEvent) {
        NewNamePanel
            .showAsDialog(name, Frame.getFrames()[0])
            ?.let {
                val directory = controller.selectedItem as LibraryDirectory
                val library = directory.library!!
                val item = FSMLibraryItem(TranslatableText(it))

                library.libraryService.addLibraryItem(library, item, directory)
                controller.eventBus.post(OpenFSMLibraryItemRequest(item))
            }
    }

    override val operationAuthorized: Boolean
        get() = operationTarget != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget!!)
}