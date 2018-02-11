package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Creates a new, empty [Graph] as a child of the currently selected [LibraryDirectory].
 */
class NewGraphAction(eventBus: EventBus) : AbstractLibraryFolderAction("library.action.newGraph", eventBus) {

    constructor(): this(BaseModule.eventBus)

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
        directory.addContainerElement(MetaGraph())
    }
}