package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.Frame
import javax.swing.JOptionPane

/**
 * An [Action] for deleting the currently selected [LibraryElement].
 */
class DeleteLibraryElementAction(
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction("graph.action.deleteLibraryElement", eventBus) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    val libraryItem = libraryTreeView!!.getSelectedItem()
	    if (JOptionPane.showConfirmDialog(
		    Frame.getFrames()[0],
		    Translations.getString("graph.action.deleteLibraryElement.question", libraryTreeView!!.getSelectedItem()!!),
		    name,
		    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
	    {
		    val library = libraryItem!!.library!!
		    library.libraryService.removeLibraryItem(libraryItem.library!!, libraryItem, folderOfSelectedItem as LibraryDirectory)
	    }
    }

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && (selectedItem is BaseLibraryElement || selectedItem is ContainerLibraryElement)
	}
}