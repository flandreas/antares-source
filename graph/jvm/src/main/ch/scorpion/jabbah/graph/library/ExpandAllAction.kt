package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.JTreeUtil
import javax.swing.tree.DefaultMutableTreeNode

/** Expands all child nodes (recursively) of the selected [LibraryDirectory].*/
class ExpandAllAction(
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction("library.action.expandAll", libraryTreeView, false, eventBus) {

	override fun calculateEnabledness(): Boolean {
		return super.calculateEnabledness() &&
			libraryTreeView.selectionPath != null &&
			(libraryTreeView.selectionPath?.lastPathComponent as DefaultMutableTreeNode).childCount > 0
	}

	override fun execute(event: ActionEvent) {
		JTreeUtil.expandAll(libraryTreeView, libraryTreeView.selectionPath)
	}
}

/** Collapses all child nodes (recursively) of the selected [LibraryDirectory].*/
class CollapseAllAction(
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction("library.action.collapseAll", libraryTreeView, false, eventBus) {

	override fun calculateEnabledness(): Boolean {
		return super.calculateEnabledness() &&
			libraryTreeView.selectionPath != null &&
			(libraryTreeView.selectionPath?.lastPathComponent as DefaultMutableTreeNode).childCount > 0
	}

	override fun execute(event: ActionEvent) {
		JTreeUtil.collapseAll(libraryTreeView, libraryTreeView.selectionPath)
	}
}