package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.Frame
import javax.swing.JOptionPane

/** An [Action] for deleting the currently selected [LibraryDirectory].*/
class DeleteLibraryFolderAction(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("graph.action.deleteLibraryDirectory", eventBus) {

	override fun calculateEnabledness(): Boolean {
		return super.calculateEnabledness() && (selectedItem as LibraryDirectory).isEmpty() && isLibraryOwnedByUser
	}

	override fun execute(event: ActionEvent) {
		val libraryItem = libraryTreeView!!.getSelectedItem()
		if (JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("graph.action.deleteLibraryDirectory.question", libraryTreeView!!.getSelectedItem()!!),
				name,
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
		{
			val library = libraryItem!!.library!!
			library.libraryService.removeLibraryItem(libraryItem.library!!, libraryItem, folderOfSelectedItem as LibraryDirectory)
		}
	}
}