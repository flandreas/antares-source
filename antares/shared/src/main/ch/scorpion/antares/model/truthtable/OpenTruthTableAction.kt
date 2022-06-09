package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.ui.GraphDataViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/** An [Action] for opening a [TruthTable] of a [TruthTableLibraryItem]. */
class OpenTruthTableAction(
	private val graphDataViewController: GraphDataViewController,
	controller: LibraryTreeViewController
) : AbstractLibraryAction("file.action.open", Operation.View, controller) {

	companion object {
		private val LOG by logger(OpenTruthTableAction::class)
	}

	private val openHandler: EventHandler<OpenTruthTableItemRequest> = {
		if (!applicationMode.isEdit()) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
		} else {
			openAsSavable(it.item)
		}
	}

	init {
		eventBus.register(OpenTruthTableItemRequest::class, openHandler)
	}

	override fun dispose() {
		eventBus.unregister(openHandler)
	}

	override fun execute(event: ActionEvent) {
		openAsSavable(controller.selectedItem as TruthTableLibraryItem)
	}

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness() && selectedItem is TruthTableLibraryItem

	private fun openAsSavable(item: TruthTableLibraryItem) {
		LOG.userTrail("Open TruthTable as main view")
		graphDataViewController.openAsStorable(item.truthTable, TruthTableSavable(item))
		eventBus.post(ShowTruthTableItemRequest(item))
	}
}