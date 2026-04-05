package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.Disposable
import io.antarescircuit.jabbah.base.collection.Stack
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.event.PropertyChangeSupport
import io.antarescircuit.jabbah.base.geom.MutableRectangularShape
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableAdapter
import io.antarescircuit.jabbah.draw.DrawableEvent
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.graph.view.EdgeView

/**
 * Manages indices of [EdgeView] points that have been adjusted (i.e. manually set by the user)
 * in a way in which they can be repetitively undone.
 */
interface EdgeViewAdjustmentModel : Disposable {

	/** The index of the [EdgeView] point that serves as layout counterpart.*/
	val current: Int

	/** The number of adjustment point indices. */
	val size: Int

	fun addListener(handler: (PropertyChangeEvent<Any>) -> Unit): PropertyChangeListener<Any>

	/** Adds the corresponding [EdgeView] endpoint index to be the new current one. */
	fun add()

	/** Removes the current endpoint index and makes the previous one the new current one. */
	fun undo()

	fun edgePointIterator(): Iterator<Point2D>
}

private abstract class AbstractEdgeViewAdjustmentModel(val edgeView: EdgeView<Any>) : EdgeViewAdjustmentModel {

	protected val pointIndices = Stack<Int>()

	private val edgeViewListener = object : DrawableAdapter() {
		override fun drawableUpdated(event: DrawableEvent) {
			var changed = false
			while (!pointIndices.empty && pointIndices.peek() > edgeView.segmentPointCount - 1) {
				pointIndices.pop()
				changed = true
			}
			if (changed) {
				fireChangeEvent()
			}
		}
	}

	init {
		edgeView.addDrawableListener(edgeViewListener)
		pointIndices.push(0)
	}

	override fun dispose() {
		edgeView.removeDrawableListener(edgeViewListener)
	}

	@Suppress("LeakingThis")
	private val changeSupport = PropertyChangeSupport<Any>(this)

	/** Returns the point index in [EdgeView] for the index in the list of adjusted points. */
	protected abstract fun getIndexAt(index: Int): Int

	protected fun fireChangeEvent() {
		changeSupport.fire("", null, null)
	}

	override val size: Int get() = pointIndices.size

	override fun addListener(handler: (PropertyChangeEvent<Any>) -> Unit): PropertyChangeListener<Any> {
		return changeSupport.add(handler)
	}

	override fun edgePointIterator(): Iterator<Point2D> {
		return object : Iterator<Point2D> {
			private var index = -1
			override fun hasNext(): Boolean = index < size - 1
			override fun next(): Point2D = edgeView.polyline.getPointAt(getIndexAt(++index))
		}
	}

	override fun undo() {
		pointIndices.pop()
		fireChangeEvent()
	}
}

/**
 * Manages the indices of the points in an [EdgeView] whose destination endpoints are being adjusted by the user.
 *
 * The [Stack] contains indices of the points in [edgeView] that have been manually set (i.e. adjusted) by the user.
 * Organized as a [Stack] to support repetitive undo by pressing ESC.
 */
private class EdgeViewDestinationAdjustmentModel(edgeView: EdgeView<Any>) : AbstractEdgeViewAdjustmentModel(edgeView) {

	override val current: Int get() = pointIndices.peek()

	override fun getIndexAt(index: Int): Int = pointIndices.getItem(index)

	override fun add() {
		pointIndices.push(edgeView.segmentPointCount - 1)
		fireChangeEvent()
		// Add another point at the same location that will be moved around
		edgeView.addSegmentPoint(edgeView.polyline.getLastPoint())
	}
}

/**
 * Manages the indices of the points in an [EdgeView] whose begin endpoints are adjusted by the user.
 *
 * The [Stack] contains the indices of the points in [edgeView] that have been manually set (i.e. adjusted) by the user,
 * counted backwards from the [EdgeView]'s end point. For example, an index with value 0 represents the last
 * point of the [EdgeView], effectively representing index "edgeView.length - 1". This reverse semantics is
 * needed to support a varying number of [EdgeView] points at the beginning of the [EdgeView].
 */
private class EdgeViewOriginAdjustmentModel(edgeView: EdgeView<Any>) : AbstractEdgeViewAdjustmentModel(edgeView) {

	override val current: Int get() = edgeView.segmentPointCount - pointIndices.peek() - 1

	override fun getIndexAt(index: Int): Int {
		return edgeView.segmentPointCount - pointIndices.getItem(index) - 1
	}

	override fun add() {
		pointIndices.push(edgeView.segmentPointCount - 1)
		fireChangeEvent()
		// Add another point at the same location that will be moved around
		edgeView.addSegmentPoint(0, edgeView.polyline.getFirstPoint())
	}
}

/** Displays adjustment points over the adjusted [EdgeView].*/
interface EdgeViewAdjustmentView : Drawable {

	val model: EdgeViewAdjustmentModel
}

/** Draws adjustment points as small filled rectangles in the selection color. */
class SimpleEdgeViewAdjustmentView(
	override val model: EdgeViewAdjustmentModel
) : AbstractDrawable(), EdgeViewAdjustmentView {

	companion object {
		private const val HALF_SIZE = 2.0

		fun forDestinationAdjustmentOf(edgeView: EdgeView<Any>): EdgeViewAdjustmentView {
			return SimpleEdgeViewAdjustmentView(EdgeViewDestinationAdjustmentModel(edgeView))
		}

		fun forOriginAdjustmentOf(edgeView: EdgeView<Any>): EdgeViewAdjustmentView {
			return SimpleEdgeViewAdjustmentView(EdgeViewOriginAdjustmentModel(edgeView))
		}
	}

	init {
		model.addListener {
			invalidate()
			updateBoundingBox()
			invalidate()
			update()
		}
	}

	override fun dispose() {
		super.dispose()
		model.dispose()
	}

	private var bbox: MutableRectangularShape = Rectangle2D()

	/** ---- [Drawable] interface */

	override val boundingBox: RectangularShape get() = Rectangle2D(bbox)

	override fun draw(context: DrawContext) {
		if (model.size > 0) {
			context.g.color = context.selectionColor?.foregroundColor ?: Color.BLACK
			model.edgePointIterator().forEach { context.g.fill(it.toRect(HALF_SIZE)) }
		}
	}

	override fun contains(x: Double, y: Double): Boolean = false

	/** ---- [SimpleEdgeViewAdjustmentView] */

	private fun updateBoundingBox() {
		bbox = Rectangle2D()
		var first = true
		model.edgePointIterator().forEach {
			if (first) {
				bbox.setFrame(it.x, it.y, 2 * HALF_SIZE, 2 * HALF_SIZE)
				first = false
			} else {
				bbox.add(it.toRect(HALF_SIZE))
			}
		}
	}
}