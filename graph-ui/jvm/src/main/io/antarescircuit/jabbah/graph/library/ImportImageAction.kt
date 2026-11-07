package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * Allows the user to select an image file, specify a logical image name,
 * and import the image into the currently selected library folder.
 */
class ImportImageAction(
    controller: LibraryTreeViewController,
) : AbstractLibraryDirectoryAction(
    "library.action.importImage",
    Operation.Change,
    controller
) {

    companion object {
        private val LOG by logger(ImportImageAction::class)
    }

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        ImportImagePanel.showAsDialog(name)?.let { params ->
            LOG.userTrail("Import image in library ${selectedFolder.library!!.uuid}")

            InvocationHandler.invoke {
                selectedFolder.library!!.libraryService.importImage(
                    params.inputPath,
                    params.type,
                    params.name,
                    selectedFolder
                )
            }
        }
    }
}