package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

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