package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Adds the current [MetaGraph] as a [ContainerLibraryElement] to the currently selected [LibraryDirectory].
 */
class AddGraphToLibraryAction(
	private val service: LibraryService = LibraryModule.libraryService.invoke(),
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.addToLibrary", eventBus) {

    private var metaGraph: MetaGraph? = null

    init {
        eventBus.register(ApplicationDataEvent::class, {
            metaGraph = it.newData as MetaGraph
        })
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
	    service.addContainerLibraryElement(libraryTreeView!!.libraryHolder.library, metaGraph!!, directory)
    }
}