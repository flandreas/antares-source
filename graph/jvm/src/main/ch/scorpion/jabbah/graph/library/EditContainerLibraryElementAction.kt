package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.action.AbstractApplicationAction
import ch.scorpion.jabbah.base.event.EventBus

/**
 * An [Action] for opening the [ContainerLibraryElement] that is currently selected in the
 * [LibraryTreeView] for editing.
 */
class EditContainerLibraryElementAction(
    application: DesktopApplication,
    eventBus: EventBus,
    private val service: LibraryService = LibraryModule.libraryService.invoke()
) : AbstractApplicationAction("graph.action.editContainerLibraryElement", application) {

    /** Holds the [LibraryTreeView] that issued the last selection event. */
    private var libraryTreeView: LibraryTreeView? = null

    init {
        enabled = false
        eventBus.register(LibrarySelectionChangedEvent::class, {
            libraryTreeView = it.libraryTreeView
            enabled = libraryTreeView!!.getSelectedItem() is ContainerLibraryElement
        })

        eventBus.register(OpenContainerLibraryElementRequest::class, {
            openAsSavable()
        })
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        openAsSavable()
    }

    /**
     * Opens the currently selected [ContainerLibraryElement] as the current [Savable] in the application.
     */
    private fun openAsSavable() {
        val element = libraryTreeView!!.getSelectedItem() as ContainerLibraryElement
	    val metaGraph = service.getMetaGraph(libraryTreeView!!.libraryHolder.library, element)
        application.open(metaGraph, LibrarySavable(metaGraph, element))
    }
}