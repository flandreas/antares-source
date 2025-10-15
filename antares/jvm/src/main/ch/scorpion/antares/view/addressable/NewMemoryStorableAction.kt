package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.antares.model.addressable.MemoryStorable
import ch.scorpion.antares.model.addressable.OpenMemoryLibraryItemRequest
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryDirectoryAction
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
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