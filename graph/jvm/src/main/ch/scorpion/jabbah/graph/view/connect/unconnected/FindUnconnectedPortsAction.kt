package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import javax.swing.JOptionPane

class FindUnconnectedPortsAction(
    controller: LibraryTreeViewController
) : AbstractFindUnconnectedPortsAction(controller) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        if (unsavedChanges()) {
            return
        }

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

    private fun unsavedChanges(): Boolean {
        if (EditModule.commandManager.canUndo()) {
            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                Translations.getString("graph.action.findUnconnectedPorts.unsavedChanged"),
                name,
                JOptionPane.ERROR_MESSAGE
            )
            return true
        }

        return false
    }
}