package io.antarescircuit.jabbah.graph.ui.desktop

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.view.ContentView
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView

/**
 * Displays content to be opened as a separate view in [GraphDesktopView]
 */
interface GraphDesktopViewItem : ContentView<EditInputEventContext> {

	/**
	 * If a [GraphDesktopViewItem] is reusable, its [disposeItem] method is NOT called
	 * when it is closed.
	 */
	val reusable: Boolean

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

	val toolBar: Any? get() = null

	/**
	 * Returns the current width of this [GraphDesktopViewItem] after being laid out within its UI parent.
	 * Used for calculating source and target areas during drag&drop actions.
	 */
	val layoutWidth: Int

	/**
	 * Returns the current height of this [GraphDesktopViewItem] after being laid out within its UI parent.
	 * Used for calculating source and target areas during drag&drop actions.
	 */
	val layoutHeight: Int

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

	fun createElementRef(verticeViewId: Int): GraphDesktopViewItemElementRef
		= GraphDesktopViewItemElementDepthRef(verticeViewId, 0)

	fun findElementWithRef(ref: GraphDesktopViewItemElementRef): VerticeView<*>? = null

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

/**
 * References an element within a [GraphDesktopViewItem] that can be
 * opened in a separate [GraphDesktopViewItem].
 */
interface GraphDesktopViewItemElementRef{
	val verticeViewId: Int
}

data class GraphDesktopViewItemElementDepthRef(
	override val verticeViewId: Int,
	val depth: Int
) : GraphDesktopViewItemElementRef