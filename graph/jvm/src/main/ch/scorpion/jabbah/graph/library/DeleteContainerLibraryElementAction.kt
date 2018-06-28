package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import javax.swing.JOptionPane
import javax.swing.tree.DefaultMutableTreeNode

/**
 * An [Action] for deleting the currently selected [ContainerLibraryElement].
 */
class DeleteContainerLibraryElementAction(
    eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementAction("graph.action.deleteContainerLibraryElement", eventBus) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    // TODO Port UI to JavaFX
	    val libraryItem = libraryTreeView!!.getSelectedItem()
	    if (JOptionPane.showConfirmDialog(
		    null,
		    Translations.getString("graph.action.deleteContainerLibraryElement.question", libraryTreeView!!.getSelectedItem()!!),
		    name,
		    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
	    {
		    val library = libraryItem!!.library!!
		    library.libraryService.removeLibraryItem(libraryItem.library!!, libraryItem, folderOfSelectedItem as LibraryDirectory)
	    }
    }
}