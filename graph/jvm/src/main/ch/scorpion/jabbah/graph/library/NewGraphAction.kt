package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import java.awt.event.ActionEvent

/**
 * Creates a new, empty [Graph] as a child of the currently selected [LibraryDirectory].
 */
class NewGraphAction(eventBus: EventBus) : AbstractLibraryFolderAction("library.action.newGraph", eventBus) {

    constructor(): this(BaseModule.eventBus)

    override fun actionPerformed(e: ActionEvent?) {
        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
        directory.addContainerElement(MetaGraph())
    }
}