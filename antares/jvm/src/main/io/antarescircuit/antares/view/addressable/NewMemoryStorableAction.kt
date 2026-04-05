package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.addressable.MemoryStorable
import io.antarescircuit.antares.model.addressable.OpenMemoryLibraryItemRequest
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractLibraryDirectoryAction
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

/**
 * Asks the user for parameters of a new [MemoryStorable] and creates it as new [MemoryLibraryItem]
 * in the currently selected [LibraryDirectory].
 */
class NewMemoryStorableAction(
    controller: LibraryTreeViewController
) : AbstractLibraryDirectoryAction(
    actionBaseName = "library.action.newMemoryStorable",
    operation = Operation.Change,
    controller
) {
    override val opensDialog: Boolean get() = true

    init {
        updateEnabled()
    }

    override fun execute(event: ActionEvent) {
        NewMemoryStorablePanel
            .showAsDialog(name, Frame.getFrames()[0])
            ?.let {
                val directory = controller.selectedItem as LibraryDirectory
                val library = directory.library!!
                val memoryLibraryItem = MemoryLibraryItem(it)

                library.libraryService.addLibraryItem(library, memoryLibraryItem, directory)
                controller.eventBus.post(OpenMemoryLibraryItemRequest(memoryLibraryItem))
            }
    }
}