package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.animation.AnimationModule
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.view.ZoomedPointTranslation
import io.antarescircuit.jabbah.draw.view.ZoomedPointVoyageAnimation
import io.antarescircuit.jabbah.draw.view.find.SearchMatch
import io.antarescircuit.jabbah.draw.view.find.SearchNavigation
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import kotlin.math.max
import kotlin.math.min

/**
 * Find in the displayed circuit or symbol, like the desktop's search bar (`SearchBarSwing` on
 * `GraphNavigationViewSwing`): a search selects all matching components (by ID, type or label, plus the nets of
 * matching tunnels, in/outs and switches), and next/previous select single matches and bring them into view.
 */
@JsExport
class WebFind internal constructor(private val ctx: WebEditorContext) {

	private companion object {
		const val FOCUS_DURATION = 400.0
	}

	private val navigation = SearchNavigation<Component>()

	/** The view the matches belong to, as navigation must not continue in a different drawing. */
	private var searchedView: DrawingView<*, *>? = null

	/** The view below the find bar: the circuit (or sub-circuit) or the symbol. */
	private val view: DrawingView<*, *>
		get() {
			val frame = ctx.frame()
			return if (frame.displayedView == GraphFrameController.DisplayedView.Container) frame.containerPanelController.drawingView
			else ctx.editor.view
		}

	/**
	 * Selects all components matching [text]; [match] is `contains`, `startsWith` or `entireWord`.
	 * Returns the result as JSON (see [result]).
	 */
	fun search(text: String, match: String, ignoreCase: Boolean): String = ctx.guarded(result()) {
		val searchMatch = when (match) {
			"startsWith" -> SearchMatch.StartsWidth
			"entireWord" -> SearchMatch.EntireWord
			else -> SearchMatch.Contains
		}
		val view = view
		EditModule.drawingViewSearchFactory.invoke().execute(view, SearchRequest(text, searchMatch, ignoreCase))
		navigation.reset(view.selectionManager.selection.sortedBy { it.id })
		searchedView = view
		ctx.notifyChanged()
		result()
	}

	/** Selects the next (or previous) match and brings it into view; returns the result as JSON. */
	fun navigate(forward: Boolean): String = ctx.guarded(result()) {
		val view = view
		if (view !== searchedView) return@guarded result()
		val match = navigation.move(forward) { view.drawing.getWithId(it.id) === it } ?: return@guarded result()
		view.selectionManager.deselectAll()
		view.selectionManager.select(match)
		focus(match, view)
		ctx.notifyChanged()
		result()
	}

	/** Forgets the matches, e.g. when the find bar closes. */
	fun reset() {
		navigation.reset(emptyList())
		searchedView = null
	}

	/** `{count, current}`: the number of matches and the one-based index of the selected one (0 while all are selected). */
	private fun result(): String {
		val r = navigation.result
		return toJson(obj {
			this.count = r.count
			this.current = r.current
		})
	}

	/**
	 * Centers [drawable] if it is not completely visible or very small at the current zoom, zooming in up to the
	 * normal size, but never so far that it no longer fits.
	 */
	private fun focus(drawable: Drawable, view: View<*>) {
		if (view.width <= 0 || view.height <= 0) return
		val box = drawable.boundingBox
		val viewBox = view.modelToView(box)
		val visible = Rectangle2D(0, 0, view.width, view.height).contains(viewBox)
		val zoom = view.zoomFactor
		val normal = view.canvas?.devicePixelRatio ?: 1.0
		val fit = if (viewBox.width > 0 && viewBox.height > 0) zoom * min(0.8 * view.width / viewBox.width, 0.8 * view.height / viewBox.height) else zoom
		val target = min(max(zoom, 0.75 * normal), fit)
		if (visible && target <= zoom) return
		val animation = ZoomedPointVoyageAnimation(view, FOCUS_DURATION, ZoomedPointTranslation(box.center, view.center, target))
		AnimationModule.constantSpeedAnimator.schedule(animation)
		animation.start()
	}
}
