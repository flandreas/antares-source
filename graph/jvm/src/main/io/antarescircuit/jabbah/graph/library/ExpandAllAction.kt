package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation.View
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

/** Expands all child nodes (recursively) of the selected [LibraryDirectory].*/
class ExpandAllAction(
	controller: LibraryTreeViewController
) : AbstractLibraryAction(
	actionBaseName = "library.action.expandAll",
	operation = View,
	controller
) {

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() &&
			controller.selectedItem is LibraryDirectory &&
			!(controller.selectedItem as LibraryDirectory).isEmpty()
	}

	override fun execute(event: ActionEvent) {
		controller.view.expandAllFromSelection()
	}
}

/** Collapses all child nodes (recursively) of the selected [LibraryDirectory].*/
class CollapseAllAction(
	controller: LibraryTreeViewController
) : AbstractLibraryAction(
	"library.action.collapseAll",
	operation = View,
	controller
) {

	override fun calculateEnabled(): Boolean {
		return super.calculateEnabled() &&
			controller.selectedItem is LibraryDirectory &&
			!(controller.selectedItem as LibraryDirectory).isEmpty()
	}

	override fun execute(event: ActionEvent) {
		controller.view.collapseAtSelection()
	}
}