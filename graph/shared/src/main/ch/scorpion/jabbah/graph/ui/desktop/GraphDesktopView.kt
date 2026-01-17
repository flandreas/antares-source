package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Displays an optional main [GraphDesktopViewItem] and multiple additional [GraphDesktopViewItem] that
 * are associated with [VerticeView]s in the main [GraphDesktopViewItem]. A typical implementation
 * might display the main [GraphDesktopViewItem] in a large area to the left side, and additional
 * [GraphDesktopViewItem]s below each other in an area to the right side.
 */
interface GraphDesktopView : UIView {

	companion object {

		/** The [Boolean] property in [ch.scorpion.jabbah.base.Properties] determining whether the "Docking" feature is active.*/
		const val PROP_DOCKING = "graph.ui.docking"
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
