package io.antarescircuit.antares.model.expression

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.library.AbstractLibraryAction
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

class OpenBooleanExpressionAction(
	private val graphDataViewController: GraphDataViewController,
	controller: LibraryTreeViewController
) : AbstractLibraryAction("file.action.open", Operation.View, controller) {

	private val openHandler: EventHandler<OpenBooleanExpressionItemRequest> = {
		if (!controller.applicationModeHolder.currentMode.isEdit()) {
			controller.eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
		} else {
			openAsSavable(it.item)
		}
	}

	init {
		controller.eventBus.register(OpenBooleanExpressionItemRequest::class, openHandler)
	}

	override fun dispose() {
		super.dispose()
		controller.eventBus.unregister(openHandler)
	}

	override fun execute(event: ActionEvent) {
		openAsSavable(controller.selectedItem as BooleanExpressionLibraryItem)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectedItem is BooleanExpressionLibraryItem

	private fun openAsSavable(item: BooleanExpressionLibraryItem) {
		graphDataViewController.openLibraryItem(item, "-", this.name)
	}
}