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
class AddLibraryFolderAction(eventBus: EventBus) : AbstractLibraryFolderAction("library.action.addFolder", eventBus) {

    constructor(): this(BaseModule.eventBus)

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val name = JOptionPane.showInputDialog(
            libraryTreeView,
            Translations.getString("library.action.addFolder.question"),
            name as String,
            JOptionPane.QUESTION_MESSAGE
        )

        if (StringUtils.isEmpty(name)) {
            return
        }

        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
        directory.addFolder(name)
    }
}