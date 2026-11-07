package io.antarescircuit.jabbah.graph.view.connect.unconnected

import io.antarescircuit.jabbah.base.Issue
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractLibraryAction
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
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