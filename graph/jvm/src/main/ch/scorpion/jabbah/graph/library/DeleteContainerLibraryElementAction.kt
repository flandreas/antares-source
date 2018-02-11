package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.RepaintingObserver.isEnabled
import javax.swing.JOptionPane
import javax.swing.tree.DefaultMutableTreeNode

/**
 * An [Action] for deleting the currently selected [ContainerLibraryElement].
 */
class DeleteContainerLibraryElementAction(
    eventBus: EventBus
) : AbstractAction("graph.action.deleteContainerLibraryElement") {

    constructor() : this(BaseModule.eventBus)

    private var libraryTreeView: LibraryTreeView? = null

    init {
        isEnabled = false
        eventBus.register(LibrarySelectionChangedEvent::class, {
            libraryTreeView = it.libraryTreeView
            isEnabled = libraryTreeView!!.getSelectedItem() is ContainerLibraryElement
        })
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    // TODO Port UI to JavaFX
	    val libraryItem = libraryTreeView!!.getSelectedItem()
	    if (JOptionPane.showConfirmDialog(
		    null,
		    Translations.getString("graph.action.deleteContainerLibraryElement.question", libraryTreeView!!.getSelectedItem()!!),
		    name,
		    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
	    {
		    val folder = (libraryTreeView!!.selectionPath.parentPath.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryDirectory
		    folder.remove(libraryItem!!)
	    }
    }
}