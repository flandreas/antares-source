package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ContentView
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Displays content to be opened as a separate view in [GraphDesktopView]
 */
interface GraphDesktopViewItem : ContentView<EditInputEventContext> {

	/**
	 * The [DrawingView] displayed by this [GraphDesktopViewItem]. Implementations that don't
	 * display a drawing can return `null`.
	 */
	val drawingView: DrawingView<GraphView>?

	/**
	 * The color used as a reference when opening this [GraphDesktopView] from a source object
	 * in another [GraphDesktopView]. Typically set by [GraphDesktopView]'s controller that knows
	 * which reference colors are not used yet.
	 */
	var contextColor: CompositeColor?

	/**
	 * `true` if this [GraphDesktopViewItem]' content is detached in terms of simulation from the source object from
	 *  which it was opened.
	 */
	val isDetached: Boolean

	/**
	 * Returns `true` if this [GraphDesktopViewItem] displays [content].
	 * Used by certain implementations to decide whether a new [GraphDesktopViewItem] instance has
	 * to be created when opening objects or after saving content.
	 */
	fun displays(content: Any?): Boolean

	/** Disposes this [GraphDesktopViewItem] and also its [DrawingView] and the [GraphView] it is currently displaying.*/
	fun disposeItem()

	/** Only has to be provided by implementations that also have a [drawingView]. Others might return `null`.*/
	fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>?

	fun createCloseRequest(): Any

	override val view: View<out EditInputEventContext>? get() = drawingView

	override val mainUI: Any? get() = this
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