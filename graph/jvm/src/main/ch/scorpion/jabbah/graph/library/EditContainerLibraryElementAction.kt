package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.ProjectSavable

/**
 * An [Action] for opening the [ContainerLibraryElement] that is currently selected in the
 * [LibraryTreeView] for editing.
 */
class EditContainerLibraryElementAction(
    private val application: DesktopApplication,
    eventBus: EventBus
) : AbstractContainerLibraryElementAction("graph.action.editContainerLibraryElement", eventBus) {

	companion object {
		private val LOG by logger(EditContainerLibraryElementAction::class)
	}

    init {
        eventBus.register(OpenContainerLibraryElementRequest::class) {
	        if (applicationMode != ApplicationMode.EDIT) {
		        eventBus.post(ComponentMessage(type =  ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
	        } else {
		        openAsSavable(it.element)
	        }
        }
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        openAsSavable()
    }

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && super.calculateEnabledness()
	}

	/**
     * Opens the currently selected [ContainerLibraryElement] as the current [Savable] in the application.
     */
    private fun openAsSavable() {
        openAsSavable(libraryTreeView!!.getSelectedItem() as ContainerLibraryElement)
    }

    private fun openAsSavable(element: ContainerLibraryElement) {

	    val library = element.library!!
	    library.libraryService.loadMetaGraph(library, element)
	    when (library) {
		    LibraryModule.libraryHolder.library -> application.open(element.metaGraph!!, LibrarySavable(element))
		    ProjectModule.projectHolder.project -> application.open(element.metaGraph!!, ProjectSavable(element))
		    else -> {
			    LOG.error("EditContainerLibraryElementAction: Inconsistent state, unknown ContainerLibraryElement, cannot open")
			    throw IllegalStateException()
		    }
	    }
    }
}