package io.antarescircuit.jabbah.edit.snap

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.DrawProperties
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.ZoomPan
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.*
import kotlin.math.abs

/**
 * Snaps at the [Component]s of a [Drawing] by using their [Snappable] interface for querying
 * their desired snap locations.
 */
class ComponentSnapper(
	val editor: Editor
) : AbstractSnapper(BaseModule.settings.getBoolean(SETTING_ENABLED, false)) {

	companion object {

		private val LOG by logger(ComponentSnapper::class)

		/** The name of the [Color] property in [DrawProperties] for the highlight color.  */
		const val PROP_SNAP_HIGHLIGHT_COLOR = "edit.snap.highlight.color"

		/** The name of the [Stroke] property in [DrawProperties] for the highlight stroke.  */
		const val PROP_SNAP_HIGHLIGHT_STROKE = "edit.snap.highlight.stroke"

		/** The name of the [Boolean] property in the [DrawProperties] for the enabled property. */
		private const val SETTING_ENABLED = "edit.componentSnap.enabled"

		private const val GRAVITY = 15.0

		private val EMPTY_BBOX = Rectangle2D()
	}

	/** ---- State  */

	/** Highlights the currently snapped x coordinate. Lazy initialized.  */
	private var highlightX: SnapHighlightX? = null

	/** Highlights the currently snapped y coordinate. Lazy initialized.  */
	private var highlightY: SnapHighlightY? = null

	override val boundingBox: RectangularShape = EMPTY_BBOX

	@Suppress("UNUSED_PARAMETER")
	override fun draw(context: DrawContext) {
		// empty
	}

	@Suppress("UNUSED_PARAMETER")
	override fun contains(x: Double, y: Double): Boolean {
		return false
	}

	/** ---- [Snapper] interface */

	override var snapEnabled: Boolean
		get() = super.snapEnabled
		set(value) {
			if (value != snapEnabled) {
				super.snapEnabled = value
				BaseModule.settings.set(SETTING_ENABLED, value)
			}
		}

	/**
	 * Calculates the x snapping offset according to the [Component] that yields the smallest
	 * snappable distance from the specified location.
	 */
	override fun doSnapX(initSnappableX: SnappableX, initDx: Double): DoSnapResult? {
		var minSnapDX = Double.MAX_VALUE
		var minSnapX = Double.MAX_VALUE
		var otherSnappableX: Array<SnappableX>
		var dx: Double
		var minSnappable: Snappable? = null

		val iter = editor.drawing.frontToBackIterator()
		while (iter.hasNext()) {
			val comp = iter.next()

			if (editor.view.selectionManager.isSelected(comp)) {
				continue
			}

			otherSnappableX = comp.snappableX
			for (i in otherSnappableX.indices) {
				if (!initSnappableX.accept(otherSnappableX[i]) || !otherSnappableX[i].accept(initSnappableX)) {
					continue
				}

				if (otherSnappableX[i].x < initDx + initSnappableX.x - GRAVITY || otherSnappableX[i].x > initDx + initSnappableX.x + GRAVITY) {
					continue
				}

				dx = otherSnappableX[i].x - (initDx + initSnappableX.x)
				if (abs(dx) < abs(minSnapDX)) {
					minSnapDX = dx
					minSnapX = otherSnappableX[i].x
					minSnappable = comp
				}
			}
		}

		return minSnappable?.let { DoSnapResult(minSnapX, it) }
	}

	/**
	 * Calculates the y snapping offset according to the [Component] that yields the smallest
	 * snappable distance from the specified location.
	 */
	override fun doSnapY(initSnappableY: SnappableY, initDy: Double): DoSnapResult? {
		var minSnapDY = Double.MAX_VALUE
		var minSnapY = Double.MAX_VALUE
		var otherSnappableY: Array<SnappableY>
		var dy: Double
		var minSnappable: Snappable? = null

		val iter = editor.drawing.frontToBackIterator()
		while (iter.hasNext()) {
			val comp = iter.next()

			if (editor.view.selectionManager.isSelected(comp)) {
				continue
			}

			otherSnappableY = comp.snappableY
			for (i in otherSnappableY.indices) {
				if (!initSnappableY.accept(otherSnappableY[i]) && !otherSnappableY[i].accept(initSnappableY)) {
					continue
				}

				if (otherSnappableY[i].y < initDy + initSnappableY.y - GRAVITY || otherSnappableY[i].y > initDy + initSnappableY.y + GRAVITY) {
					continue
				}

				dy = otherSnappableY[i].y - (initDy + initSnappableY.y)
				if (abs(dy) < abs(minSnapDY)) {
					minSnapDY = dy
					minSnapY = otherSnappableY[i].y
					minSnappable = comp
				}
			}
		}

		return minSnappable?.let { DoSnapResult(minSnapY, it) }
	}

	override fun getSnapHighlightX(x: Double, y: Double, snappable: Snappable?): Unzoomable? {
		LOG.trace("getSnapHighlightX for $x")

		if (snappable != null) {
			val snappableHighlight = snappable.getSnapHighlightX(x, y)
			if (snappableHighlight != null) {
				return snappableHighlight
			}
		}

		// Return default highlight
		if (highlightX == null) {
			highlightX = SnapHighlightX()
		}
		highlightX!!.setPositionX(x)
		return highlightX
	}

	override fun getSnapHighlightY(x: Double, y: Double, snappable: Snappable?): Unzoomable? {
		LOG.trace("getSnapHighlightX for $y")

		if (snappable != null) {
			val snappableHighlight = snappable.getSnapHighlightY(x, y)
			if (snappableHighlight != null) {
				return snappableHighlight
			}
		}

		if (highlightY == null) {
			highlightY = SnapHighlightY()
		}
		highlightY!!.setPositionY(y)
		return highlightY
	}

	/** ---- [ComponentSnapper]  */

	internal inner class SnapHighlightX : AbstractDrawable(), Unzoomable {

		/** ---- State  */

		/** Holds the snapped x coordinate in model space.  */
		private var positionX: Double = 0.toDouble()

		override var zoomPan: ZoomPan? = null

		private val _boundingBox = Rectangle2D()
		override val boundingBox: RectangularShape get() = _boundingBox

		/** ---- [Drawable] interface  */

		override fun draw(context: DrawContext) {
			val oldColor = context.g.color
			val oldStroke = context.g.stroke
			context.g.color = DrawModule.properties.getColor(PROP_SNAP_HIGHLIGHT_COLOR)
			context.g.stroke = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE)

			val begin = editor.view.modelToView(Point2D(positionX, 0.0))
			context.g.drawLine(
				begin.x.toInt(),
				0,
				begin.x.toInt(),
				editor.view.height)

			context.g.color = oldColor
			context.g.stroke = oldStroke
		}

		override fun contains(x: Double, y: Double): Boolean {
			return false
		}

		/** ---- [SnapHighlightX]  */

		/**
		 * Sets the snapped x coordinate.
		 */
		fun setPositionX(x: Double) {
			val lineWidth = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE).width
			invalidate()

			positionX = x
			_boundingBox.setFrame(
				positionX - lineWidth / 2,
				editor.view.viewToModelY(0.0),
				lineWidth.toDouble(),
				editor.view.viewToModelY(editor.view.height.toDouble()) - editor.view.viewToModelY(0.0))

			invalidate()
		}
	}

	internal inner class SnapHighlightY : AbstractDrawable(), Unzoomable {

		/** ---- State  */

		/** Holds the snapped y coordinate in model space.  */
		private var positionY: Double = 0.toDouble()

		override var zoomPan: ZoomPan? = null

		private val _boundingBox = Rectangle2D()
		override val boundingBox: RectangularShape get() = _boundingBox

		/** ---- [Drawable] interface  */

		override fun draw(context: DrawContext) {
			val oldColor = context.g.color
			val oldStroke = context.g.stroke
			context.g.color = DrawModule.properties.getColor(PROP_SNAP_HIGHLIGHT_COLOR)
			context.g.stroke = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE)

			val begin = editor.view.modelToView(Point2D(0.0, positionY))
			context.g.drawLine(
				0,
				begin.y.toInt(),
				editor.view.width,
				begin.y.toInt())

			context.g.color = oldColor
			context.g.stroke = oldStroke
		}

		override fun contains(x: Double, y: Double): Boolean {
			return false
		}

		/** ---- [SnapHighlightX]  */

		/**
		 * Sets the snapped x coordinate.
		 */
		fun setPositionY(y: Double) {
			val lineWidth = DrawModule.properties.getStroke(PROP_SNAP_HIGHLIGHT_STROKE).width
			invalidate()

			positionY = y
			_boundingBox.setFrame(
				editor.view.viewToModelX(0.0),
				positionY - lineWidth / 2,
				editor.view.viewToModelX(editor.view.width.toDouble()) - editor.view.viewToModelX(0.0),
				lineWidth.toDouble())

			invalidate()
		}
	}
}