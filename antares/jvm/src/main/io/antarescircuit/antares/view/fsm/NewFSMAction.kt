package io.antarescircuit.antares.view.fsm

import io.antarescircuit.antares.model.fsm.FSMLibraryItem
import io.antarescircuit.antares.model.fsm.OpenFSMLibraryItemRequest
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.ui.NewNamePanel
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.library.AbstractLibraryDirectoryAction
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
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
        updateEnabled()
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