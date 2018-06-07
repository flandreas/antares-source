package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.action.AbstractApplicationAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.ProjectSavable

/**
 * An [Action] for opening the [ContainerLibraryElement] that is currently selected in the
 * [LibraryTreeView] for editing.
 * TODO The logic implemented by this Action should rather be in a business domain libraryService.
 */
class EditContainerLibraryElementAction(
    application: DesktopApplication,
    eventBus: EventBus,
    private val libraryService: LibraryService = LibraryModule.libraryService.invoke(),
    private val projectService: LibraryService = ProjectModule.projectLibraryService.invoke()
) : AbstractApplicationAction("graph.action.editContainerLibraryElement", application) {

	companion object {
		private val LOG by logger(EditContainerLibraryElementAction::class)
	}

    /** Holds the [LibraryTreeView] that issued the last selection event. */
    private var libraryTreeView: LibraryTreeView? = null

    init {
        enabled = false
        eventBus.register(LibrarySelectionChangedEvent::class, {
            libraryTreeView = it.libraryTreeView
            enabled = libraryTreeView!!.getSelectedItem() is ContainerLibraryElement
        })

        eventBus.register(OpenContainerLibraryElementRequest::class, {
            openAsSavable(it.element)
        })
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        openAsSavable()
    }

    /**
     * Opens the currently selected [ContainerLibraryElement] as the current [Savable] in the application.
     */
    private fun openAsSavable() {
        openAsSavable(libraryTreeView!!.getSelectedItem() as ContainerLibraryElement)
    }

    private fun openAsSavable(element: ContainerLibraryElement) {
	    val library = element.library!!
	    val metaGraph = library.libraryService.getMetaGraph(library, element)
	    if (library == LibraryModule.libraryHolder.library) {
		    application.open(metaGraph, LibrarySavable(metaGraph, element))
	    } else if (library == ProjectModule.projectHolder.project) {
		    application.open(metaGraph, ProjectSavable(metaGraph, element))
	    } else {
		    LOG.error("EditContainerLibraryElementAction: Inconsistent state, unknown ContainerLibraryElement, cannot open")
		    throw IllegalStateException()
	    }
    }
}