package io.antarescircuit.jabbah.graph.poster

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractLibraryAction
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

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