package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class FindUnconnectedPortsAction(
    controller: LibraryTreeViewController
) : AbstractFindUnconnectedPortsAction(controller) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        val type = FindUnconnectedPortsPanel.showAsDialog()
        if (type != null) {
            val metaGraph = LibraryModule.libraryHolder.library.getMetaGraph((selectedItem as ContainerLibraryElement).uuid)

            InvocationHandler.invoke {
                val results = FindUnconnectedPortsService.findInMetaGraph(metaGraph, type)
                if (results.isNotEmpty()) {
                    FindUnconnectedPortsService.postAsIssues(results, eventBus, this::handleIssue)
                } else {
                    showNothingFoundMessage(type)
                }
            }
        }
    }
}