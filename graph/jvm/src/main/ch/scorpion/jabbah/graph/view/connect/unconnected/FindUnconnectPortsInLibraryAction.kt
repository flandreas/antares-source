package ch.scorpion.jabbah.graph.view.connect.unconnected

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import javax.swing.JFrame
import javax.swing.JOptionPane

class FindUnconnectPortsInLibraryAction(
    controller: LibraryTreeViewController
) : AbstractLibraryAction("graph.action.findUnconnectedPorts", Operation.View, controller) {

    override val opensDialog: Boolean get() = true

    override fun calculateEnabled(): Boolean = super.calculateEnabled() && selectedItem is Library

    override fun execute(event: ActionEvent) {
        val type = FindUnconnectedPortsPanel.showAsDialog()
        if (type != null) {
            InvocationHandler.invoke {
                val results = FindUnconnectedPortsService.findInLibrary(selectedItem as Library, type)
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