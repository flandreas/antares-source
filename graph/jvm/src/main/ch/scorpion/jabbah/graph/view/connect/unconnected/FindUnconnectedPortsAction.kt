package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import javax.swing.JFrame
import javax.swing.JOptionPane

class FindUnconnectedPortsAction(
    controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction("graph.action.findUnconnectedPorts", Operation.View, controller) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        val type = FindUnconnectedPortsPanel.showAsDialog()
        if (type != null) {
            val metaGraph = LibraryModule.libraryHolder.library.getMetaGraph((selectedItem as ContainerLibraryElement).uuid)

            InvocationHandler.invoke {
                val results = FindUnconnectedPortsService.findInMetaGraph(metaGraph, type)
                if (results.isNotEmpty()) {
                    FindUnconnectedPortsService.postAsIssues(results, eventBus)
                } else {
                    JOptionPane.showConfirmDialog(
                        JFrame.getFrames()[0],
                        Translations.getString("graph.action.findUnconnectedPorts.none.text", type.toString()),
                        this.name,
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                    )

                }
            }
        }
    }
}