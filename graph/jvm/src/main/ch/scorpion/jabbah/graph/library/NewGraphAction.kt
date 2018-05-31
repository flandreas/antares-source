package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Creates a new, empty [Graph] as a child of the currently selected [LibraryDirectory].
 */
class NewGraphAction(
	private val service: LibraryService = LibraryModule.libraryService.invoke(),
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.newGraph", eventBus) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
	    service.addContainerLibraryElement(libraryTreeView!!.libraryHolder.library, MetaGraph(), directory)
    }
}