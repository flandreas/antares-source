package io.antarescircuit.jabbah.graph.ui.library

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController

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

	override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
		openAsSavable()
	}

	/**
	 * Opens the currently selected [ContainerLibraryElement] as the current [Savable] in the application.
	 */
	private fun openAsSavable() {
		openAsSavable(controller.selectedItem as ContainerLibraryElement, null)
	}

	private fun openAsSavable(element: ContainerLibraryElement, focusVerticeViewId: Int?) {
		if (graphDataViewController.metaGraph?.uuid == element.uuid) {
			controller.eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.open.alreadyOpen.msg"))
			return
		}
		graphDataViewController.openAsSavable(element, name, focusVerticeViewId)
	}
}