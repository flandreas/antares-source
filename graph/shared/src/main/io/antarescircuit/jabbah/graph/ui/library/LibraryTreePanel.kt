package io.antarescircuit.jabbah.graph.ui.library

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.graph.library.LibraryFilter
import io.antarescircuit.jabbah.graph.library.LibraryItem

interface LibraryTreePanel : UIView {

	/**
	 * Restricts the displayed [LibraryTreeView] to the [LibraryItem]s
	 * filtered by [filter]. If `null`, no restrictions are applied.
	 */
	fun filter(filter: LibraryFilter?)

	fun clearFilter()
}

/**
 * Contains a [LibraryTreeView] and a search field for filtering the displayed nodes.
 */
class LibraryTreePanelController(
	val libraryTreeViewController: LibraryTreeViewController
): AbstractUIController<LibraryTreePanel>() {

	val locateMetaGraphAction: Action = LocateMetaGraphAction(this)

	override fun dispose() {
		super.dispose()
		locateMetaGraphAction.dispose()
	}

	/**
	 * Uses [text] as a filter text for filtering the displayed contents of [LibraryTreePanel].
	 */
	fun search(text: String?) {
		if (StringUtils.isBlank(text)) {
			view.filter(null)
		} else {
			view.filter { item -> item.toString().contains(text!!, true) }
		}
	}

	fun locateMetaGraph(metaGraph: UUID) {
		view.clearFilter()
		libraryTreeViewController.expandTo(metaGraph)
	}
}