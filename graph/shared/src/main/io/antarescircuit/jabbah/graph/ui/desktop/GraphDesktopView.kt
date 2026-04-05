package io.antarescircuit.jabbah.graph.ui.desktop

import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Displays an optional main [GraphDesktopViewItem] and multiple additional [GraphDesktopViewItem] that
 * are associated with [VerticeView]s in the main [GraphDesktopViewItem]. A typical implementation
 * might display the main [GraphDesktopViewItem] in a large area to the left side, and additional
 * [GraphDesktopViewItem]s below each other in an area to the right side.
 */
interface GraphDesktopView : UIView {

	companion object {

		/** The [Boolean] property in [io.antarescircuit.jabbah.base.Properties] determining whether the "Docking" feature is active.*/
		const val PROP_DOCKING = "graph.ui.docking"

		/** The [Int] property in [io.antarescircuit.jabbah.base.Properties] the number of rows per column when using "Docking". */
		const val PROP_ROWS_PER_COLUMN = "graph.ui.docking.rowsPerColumn"
	}

	fun createSubGraphDesktopItem(
		verticeView: SubGraphVerticeView<*>,
		referenceColor: CompositeColor?,
		isParentDetached: Boolean,
		viewManager: ContentViewManager
	): GraphDesktopViewItem

	/** Closes all open [GraphDesktopViewItem]s and shows [item] as the main [GraphDesktopViewItem]. */
	fun showMainItem(item: GraphDesktopViewItem)

	/** Adds a child [GraphDesktopViewItem] to the right side of this [GraphDesktopView].*/
	fun showChildItem(item: GraphDesktopViewItem)

	fun closeChildItem(item: GraphDesktopViewItem)

	/** Closes all open [GraphDesktopViewItem]s. */
	fun closeAll()
}
