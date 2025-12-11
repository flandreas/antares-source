package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

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