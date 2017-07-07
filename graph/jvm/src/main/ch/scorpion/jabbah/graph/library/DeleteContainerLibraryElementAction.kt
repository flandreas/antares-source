package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JOptionPane
import javax.swing.tree.DefaultMutableTreeNode

/**
 * An [Action] for deleting the currently selected [ContainerLibraryElement].
 */
class DeleteContainerLibraryElementAction(
    eventBus: EventBus
) : AbstractAction("graph.action.deleteContainerLibraryElement") {

    private var libraryTreeView: LibraryTreeView? = null

    init {
        enabled = false
        eventBus.register(LibrarySelectionChangedEvent::class, {
            libraryTreeView = it.libraryTreeView
            enabled = libraryTreeView!!.getSelectedItem() is LibraryDirectory
        })
    }

    override fun actionPerformed(e: ActionEvent?) {
        val libraryItem = libraryTreeView!!.getSelectedItem()
        if (JOptionPane.showConfirmDialog(
            null,
            Translations.getString("graph.action.deleteContainerLibraryElement.question", libraryTreeView!!.getSelectedItem()!!),
            getValue(Action.NAME) as String,
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
        {
            val folder = (libraryTreeView!!.selectionPath.parentPath.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryFolder
            folder.remove(libraryItem!!)
        }
    }
}