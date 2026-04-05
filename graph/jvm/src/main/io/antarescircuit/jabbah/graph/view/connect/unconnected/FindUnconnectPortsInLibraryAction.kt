package io.antarescircuit.jabbah.graph.view.connect.unconnected

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

class FindUnconnectPortsInLibraryAction(
    controller: LibraryTreeViewController
) : AbstractFindUnconnectedPortsAction(controller) {

    override val opensDialog: Boolean get() = true

    override fun calculateEnabled(): Boolean = super.calculateEnabled() && selectedItem is Library

    override fun execute(event: ActionEvent) {
        val type = FindUnconnectedPortsPanel.showAsDialog()
        if (type != null) {
            InvocationHandler.invoke {
                val results = FindUnconnectedPortsService.findInLibrary(selectedItem as Library, type)
                if (results.isNotEmpty()) {
                    FindUnconnectedPortsService.postAsIssues(results, eventBus, this::handleIssue)
                } else {
                    showNothingFoundMessage(type)
                }
            }
        }
    }
}