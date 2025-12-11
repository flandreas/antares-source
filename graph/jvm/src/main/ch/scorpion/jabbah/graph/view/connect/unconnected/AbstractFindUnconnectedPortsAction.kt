package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.Issue
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import javax.swing.JFrame
import javax.swing.JOptionPane

abstract class AbstractFindUnconnectedPortsAction(
    controller: LibraryTreeViewController
) : AbstractLibraryAction("graph.action.findUnconnectedPorts", Operation.View, controller) {

    protected fun handleIssue(issue: Issue) {
        val up = issue.data as UnconnectedPort
        LibraryModule.libraryHolder.library.getContainerLibraryElement(up.metaGraphId)?.let {
            BaseModule.eventBus.post(OpenContainerLibraryElementRequest(it, up.verticeViewId))
        }
    }

    protected fun showNothingFoundMessage(type: FindUnconnectedPortsType) {
        JOptionPane.showConfirmDialog(
            JFrame.getFrames()[0],
            Translations.getString("graph.action.findUnconnectedPorts.none.text", type.toString()),
            this.name,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        )
    }
}