package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class OpenBooleanExpressionAction(
	private val graphDataViewController: GraphDataViewController,
	controller: LibraryTreeViewController
) : AbstractLibraryAction("file.action.open", Operation.View, controller) {

	companion object {
		private val LOG by logger(OpenBooleanExpressionAction::class)
	}

	private val openHandler: EventHandler<OpenBooleanExpressionItemRequest> = {
		if (!applicationMode.isEdit()) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
		} else {
			openAsSavable(it.item)
		}
	}

	init {
		eventBus.register(OpenBooleanExpressionItemRequest::class, openHandler)
	}

	override fun dispose() {
		eventBus.unregister(openHandler)
	}

	override fun execute(event: ActionEvent) {
		openAsSavable(controller.selectedItem as BooleanExpressionLibraryItem)
	}

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness() && selectedItem is BooleanExpressionLibraryItem

	private fun openAsSavable(item: BooleanExpressionLibraryItem) {
		LOG.debug("Open BooleanExpression as main view")
		graphDataViewController.openAsStorable(item, BooleanExpressionSavable(item))
		eventBus.post(ShowBooleanExpressionItemRequest(item))
	}
}