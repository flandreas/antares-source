package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType

/**
 * An [Action] for opening the [ContainerLibraryElement] that is currently selected in the
 * [LibraryTreeView] for editing.
 */
class OpenContainerLibraryElementAction(
	private val application: DesktopApplication,
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractContainerLibraryElementAction("graph.action.openContainerLibraryElement", libraryTreeView, eventBus) {

	init {
		eventBus.register(OpenContainerLibraryElementRequest::class) {
			if (!applicationMode.isEdit()) {
				eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
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
		openAsSavable(libraryTreeView.getSelectedItem() as ContainerLibraryElement)
	}

	private fun openAsSavable(element: ContainerLibraryElement) {
		val library = element.library!!
		library.libraryService.loadMetaGraph(library, element)
		application.open(element.metaGraph!!, library.createSavable(element))
	}
}