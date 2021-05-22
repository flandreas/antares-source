package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * A base class for implementing rectangular [Drawable]s of fixed size that implement the [Unzoomable]
 * interface.
 *
 * The location of an [AbstractRectangularUnzoomable] is its center.
 */
abstract class AbstractRectangularUnzoomable(
	halfSize: Double,
	location: Point2D = Point2D.ZERO
) : AbstractDrawable(), Unzoomable {

	override var zoomPan: ZoomPan? = ZoomPan()

	/** Holds the center of the rectangle. */
	var location: Point2D = location
		set(value) {
			invalidate()
			field = Point2D(value)
			invalidate()
			update()
		}

	var halfSize: Double = halfSize
		set(value) {
			invalidate()
			field = value
			invalidate()
			update()
		}

	/** Holds the bounding box in model coordinate space. */
	private val bboxModel = Rectangle2D()

	/** Contains the width of the outline. Used for bounding box calculation. */
	abstract val lineWidth: Double

	override val boundingBox: Rectangle2D
		get() {
			bboxModel.setFrame(
				location.x - halfSize / zoomPan!!.zoomFactor - lineWidth,
				location.y - halfSize / zoomPan!!.zoomFactor - lineWidth,
				2 * (halfSize / zoomPan!!.zoomFactor + lineWidth),
				2 * (halfSize / zoomPan!!.zoomFactor + lineWidth)
			)
			return bboxModel
		}

	override fun contains(x: Double, y: Double): Boolean {
		return x >= location.x - halfSize / zoomPan!!.zoomFactor
			&& x <= location.x + halfSize / zoomPan!!.zoomFactor
			&& y >= location.y - halfSize / zoomPan!!.zoomFactor
			&& y <= location.y + halfSize / zoomPan!!.zoomFactor
	}

	/** Returns the rectangle in view coordinate space.*/
	protected fun getViewRectangle(): Rectangle2D {
		val p = zoomPan!!.transform.modelToView(location)
		return Rectangle2D(p.x - halfSize, p.y - halfSize, 2 * halfSize, 2 * halfSize)
	}
}