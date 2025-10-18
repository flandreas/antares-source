package ch.scorpion.jabbah.graph.poster

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class CreatePosterAction(
    private val applicationName: String,
    controller: LibraryTreeViewController
) : AbstractLibraryAction("graph.action.poster.action", Operation.View, controller) {

    companion object {
        private val LOG by logger(CreatePosterAction::class)
    }

    override val opensDialog: Boolean get() = true

    override fun calculateEnabled(): Boolean {
        return super.calculateEnabled() && selectedItem is Library
    }

    override fun execute(event: ActionEvent) {
        LOG.userTrail("Create poster of project ${(selectedItem as Library).uuid.id}")
        InvocationHandler.invoke {
            PosterViewerSwing(applicationName, selectedItem as Library)
        }
    }
}