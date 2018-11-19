package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.Frame
import javax.swing.JOptionPane

/**
 * Asks the user for the name of the new [LibraryDirectory] and adds a new [LibraryDirectory] as a child of
 * the currently selected [LibraryDirectory].
 */
class AddLibraryFolderAction(
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.addFolder", eventBus) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val name = JOptionPane.showInputDialog(
	        Frame.getFrames()[0],
            Translations.getString("library.action.addFolder.question"),
	        name,
            JOptionPane.QUESTION_MESSAGE
        )

        if (StringUtils.isEmpty(name)) {
            return
        }

        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
	    directory.library!!.libraryService.addFolder(directory.library!!, name, directory)
    }

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && super.calculateEnabledness()
	}
}