package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JOptionPane

/**
 * Asks the user for the name of the new [LibraryDirectory] and adds a new [LibraryDirectory] as a child of
 * the currently selected [LibraryDirectory].
 */
class AddLibraryFolderAction(eventBus: EventBus) : AbstractLibraryFolderAction("library.action.addFolder", eventBus) {

    constructor(): this(BaseModule.eventBus)

    override fun actionPerformed(e: ActionEvent?) {
        val name = JOptionPane.showInputDialog(
            libraryTreeView,
            Translations.getString("library.action.addFolder.question"),
            getValue(Action.NAME) as String,
            JOptionPane.QUESTION_MESSAGE
        )

        if (StringUtils.isEmpty(name)) {
            return
        }

        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
        directory.addFolder(name)
    }
}