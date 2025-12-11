package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest
import ch.scorpion.jabbah.graph.ui.GraphDataViewController

/**
 * An [Action] for opening the [ContainerLibraryElement] that is currently selected in the
 * [LibraryTreeViewController] for viewing. Whether it can be edited is decided by the view that displays it.
 */
class OpenContainerLibraryElementAction(
	private val graphDataViewController: GraphDataViewController,
	controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction(
	actionBaseName = "graph.action.openContainerLibraryElement",
	operation = Operation.View,
	controller
) {

	private val openHandler: EventHandler<OpenContainerLibraryElementRequest> = {
		if (!controller.applicationModeHolder.currentMode.isEdit()) {
			controller.eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
		} else {
			openAsSavable(it.element, it.focusVerticeViewId)
		}
	}

	init {
		controller.eventBus.register(OpenContainerLibraryElementRequest::class, openHandler)
	}

	override fun dispose() {
		super.dispose()
		controller.eventBus.unregister(openHandler)
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		openAsSavable()
	}

	/**
	 * Opens the currently selected [ContainerLibraryElement] as the current [Savable] in the application.
	 */
	private fun openAsSavable() {
		openAsSavable(controller.selectedItem as ContainerLibraryElement, null)
	}

	private fun openAsSavable(element: ContainerLibraryElement, focusVerticeViewId: Int?) {
		graphDataViewController.openAsSavable(element, name, focusVerticeViewId)
	}
}