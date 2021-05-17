package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.*
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

	override val boundingBox: Rectangle2D = EMPTY_BBOX

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
	override fun doSnapX(initSnappableX: SnappableX, initDx: Double): Double {
		var minSnapDX = Double.MAX_VALUE
		var minSnapX = Double.MAX_VALUE
		var otherSnappableX: Array<SnappableX>
		var dx: Double

		val iter = editor.drawing.frontToBackIterator()
		while (iter.hasNext()) {
			val comp = iter.next()

			if (editor.view.selectionManager.isSelected(comp)) {
				continue
			}

			otherSnappableX = comp.snappableX
			for (i in otherSnappableX.indices) {
				if (!initSnappableX.accept(otherSnappableX[i])) {
					continue
				}

				if (otherSnappableX[i].x < initDx + initSnappableX.x - GRAVITY || otherSnappableX[i].x > initDx + initSnappableX.x + GRAVITY) {
					continue
				}

				dx = otherSnappableX[i].x - (initDx + initSnappableX.x)
				if (abs(dx) < abs(minSnapDX)) {
					minSnapDX = dx
					minSnapX = otherSnappableX[i].x
				}
			}
		}

		return minSnapX
	}

	/**
	 * Calculates the y snapping offset according to the [Component] that yields the smallest
	 * snappable distance from the specified location.
	 */
	override fun doSnapY(initSnappableY: SnappableY, initDy: Double): Double {
		var minSnapDY = Double.MAX_VALUE
		var minSnapY = Double.MAX_VALUE
		var otherSnappableY: Array<SnappableY>
		var dy: Double

		val iter = editor.drawing.frontToBackIterator()
		while (iter.hasNext()) {
			val comp = iter.next()

			if (editor.view.selectionManager.isSelected(comp)) {
				continue
			}

			otherSnappableY = comp.snappableY
			for (i in otherSnappableY.indices) {
				if (!initSnappableY.accept(otherSnappableY[i])) {
					continue
				}

				if (otherSnappableY[i].y < initDy + initSnappableY.y - GRAVITY || otherSnappableY[i].y > initDy + initSnappableY.y + GRAVITY) {
					continue
				}

				dy = otherSnappableY[i].y - (initDy + initSnappableY.y)
				if (abs(dy) < abs(minSnapDY)) {
					minSnapDY = dy
					minSnapY = otherSnappableY[i].y
				}
			}
		}

		return minSnapY
	}

	override fun getSnapHighlightX(x: Double, y: Double): Unzoomable? {
		LOG.trace("getSnapHighlightX for $x")
		if (highlightX == null) {
			highlightX = SnapHighlightX()
		}
		highlightX!!.setPositionX(x)
		return highlightX
	}

	override fun getSnapHighlightY(x: Double, y: Double): Unzoomable? {
		LOG.trace("getSnapHighlightX for $y")
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

		override val boundingBox: Rectangle2D = Rectangle2D()

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
			boundingBox.setFrame(
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

		override val boundingBox = Rectangle2D()

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
			boundingBox.setFrame(
				editor.view.viewToModelX(0.0),
				positionY - lineWidth / 2,
				editor.view.viewToModelX(editor.view.width.toDouble()) - editor.view.viewToModelX(0.0),
				lineWidth.toDouble())

			invalidate()
		}
	}
}