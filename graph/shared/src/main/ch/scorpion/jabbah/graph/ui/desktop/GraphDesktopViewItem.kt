package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.view.GraphView

/** Displays a [DrawingView] within a separate [GraphDesktopViewItem].*/
interface GraphDesktopViewItem {

	val drawingView: DrawingView<GraphView>?

	var contextColor: CompositeColor?

	val isDetached: Boolean

	/** Disposes this [GraphDesktopViewItem] and also its [DrawingView] and the [GraphView] it is currently displaying.*/
	fun disposeItem()

	fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>?

	fun createCloseRequest(): Any
}

/**
 * A question request to be used in [EventBus.postTwoPhase] for checking if a [GraphDesktopViewItem]
 * can safely be closed.
 */
data class GraphDesktopViewItemCloseQuestion(
	val item: GraphDesktopViewItem,
	val isRoot: Boolean
)

/**
 * A request to close a [GraphDesktopViewItem] posted on an [EventBus].
 * Should always be proceeded by [GraphDesktopViewItemCloseQuestion]
 *
 * @param item the [GraphDesktopViewItem] the user has requested to close
 * @param isRoot `true` if [item] displays data associated with the main [Savable], which the user might have changed
 */
data class GraphDesktopViewItemCloseRequest(
	val item: GraphDesktopViewItem,
	val isRoot: Boolean
)