package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import javax.swing.JOptionPane

/**
 * Asks the user for the name of the new [LibraryDirectory] and adds a new [LibraryDirectory] as a child of
 * the currently selected [LibraryDirectory].
 */
class AddLibraryFolderAction(
	private val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.addFolder", eventBus) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val name = JOptionPane.showInputDialog(
            libraryTreeView,
            Translations.getString("library.action.addFolder.question"),
	        name,
            JOptionPane.QUESTION_MESSAGE
        )

        if (StringUtils.isEmpty(name)) {
            return
        }

        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
	    libraryService.addFolder(libraryTreeView!!.libraryHolder.library, name, directory)
    }
}