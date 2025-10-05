package ch.scorpion.antares.view.fsm

import ch.scorpion.antares.model.fsm.FSMLibraryItem
import ch.scorpion.antares.model.fsm.OpenFSMLibraryItemRequest
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.ui.NewNamePanel
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.AbstractLibraryDirectoryAction
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

class NewFSMAction(
    controller: LibraryTreeViewController
) : AbstractLibraryDirectoryAction(
    actionBaseName = "library.action.newFSM",
    operation = Operation.Change,
    controller
) {

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
}