package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.edit.auth.Operation.View
import ch.scorpion.jabbah.graph.ui.LibraryTreeViewController

/** Expands all child nodes (recursively) of the selected [LibraryDirectory].*/
class ExpandAllAction(
	controller: LibraryTreeViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction(
	actionBaseName = "library.action.expandAll",
	operation = View,
	controller,
	eventBus
) {

	override fun calculateEnabledness(): Boolean {
		return super.calculateEnabledness() &&
			controller.selectedItem is LibraryDirectory &&
			!(controller.selectedItem as LibraryDirectory).isEmpty()
	}

	override fun execute(event: ActionEvent) {
		controller.view.expandAllFromSelection()
	}
}

/** Collapses all child nodes (recursively) of the selected [LibraryDirectory].*/
class CollapseAllAction(
	controller: LibraryTreeViewController,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction(
	"library.action.collapseAll",
	operation = Change,
	controller,
	eventBus
) {

	override fun calculateEnabledness(): Boolean {
		return super.calculateEnabledness() &&
			controller.selectedItem is LibraryDirectory &&
			!(controller.selectedItem as LibraryDirectory).isEmpty()
	}

	override fun execute(event: ActionEvent) {
		controller.view.collapseAtSelection()
	}
}