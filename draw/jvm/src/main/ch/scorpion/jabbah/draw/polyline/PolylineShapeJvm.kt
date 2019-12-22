package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.draw.graphics.Graphics2D
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.*

actual object PolylineShapeFactory {

	actual fun create(points: List<ch.scorpion.jabbah.base.geom.Point2D>?): PolylineShape = PolylineShapeJvm(points)

}
/**
 * Adapts [PolylineShape] to the [java.awt.Shape] interface.
 */
class PolylineShapeJvm private constructor(val shape: PolylineShape) : PolylineShape by shape, java.awt.Shape {

	constructor() : this(PolylineShapeImpl())
	constructor(points: List<ch.scorpion.jabbah.base.geom.Point2D>?) : this(PolylineShapeImpl(points ?: listOf()))

	/** ---- [java.awt.Shape] interface */

	override fun contains(p: Point2D?): Boolean {
		if (p == null) {
			return false
		}
		return shape.contains(p.x, p.y)
	}

	override fun contains(r: Rectangle2D?): Boolean {
		if (r == null) {
			return false
		}
		return shape.contains(r.x, r.y, r.width, r.height)
	}

	override fun intersects(r: Rectangle2D?): Boolean {
		if (r == null) {
			return false
		}
		return shape.intersects(r.x, r.y, r.width, r.height)
	}

	override fun getBounds2D(): Rectangle2D {
		val r = shape.boundingBox
		return Rectangle2D.Double(r.x, r.y, r.width, r.height)
	}

	override fun getPathIterator(at: AffineTransform?): PathIterator {
		return PolylineIterator(at)
	}

	override fun getPathIterator(at: AffineTransform?, flatness: Double): PathIterator {
		return PolylineIterator(at)
	}

	override fun getBounds(): Rectangle {
		val r = shape.boundingBox
		return Rectangle(r.x.toInt(), r.y.toInt(), r.width.toInt(), r.height.toInt())
	}


	/**
	 * This class is needed by [Graphics2D] in the rendering process.
	 */
	private inner class PolylineIterator(private val affine: AffineTransform?) : PathIterator {

		/** Holds the index of the current segment. Starts with 0.  */
		private var index: Int = 0

		init {
			index = 0
		}

		/** ---- [PathIterator]  */

		override fun currentSegment(coords: DoubleArray): Int {
			if (isDone) {
				throw NoSuchElementException("polyline iterator out of bounds")
			}

			val current = if (index == 0 && beginLineTerminator != null) {
				beginLineTerminator!!.lineEnd
			} else if (index == shape.pointsCount - 1 && endLineTerminator != null) {
				endLineTerminator!!.lineEnd
			} else {
				shape.getPointAt(index)
			}

			coords[0] = current.x
			coords[1] = current.y

			affine?.transform(coords, 0, coords, 0, 1)
			return if (index == 0) PathIterator.SEG_MOVETO else PathIterator.SEG_LINETO
		}

		override fun currentSegment(coords: FloatArray): Int {
			if (isDone) {
				throw NoSuchElementException("polyline iterator out of bounds")
			}
			val current = if (index == 0 && beginLineTerminator != null) {
				beginLineTerminator!!.lineEnd
			} else if (index == shape.pointsCount - 1 && endLineTerminator != null) {
				endLineTerminator!!.lineEnd
			} else {
				shape.getPointAt(index)
			}

			coords[0] = current.x.toFloat()
			coords[1] = current.y.toFloat()

			affine?.transform(coords, 0, coords, 0, 1)
			return if (index == 0) PathIterator.SEG_MOVETO else PathIterator.SEG_LINETO
		}

		override fun getWindingRule(): Int {
			return PathIterator.WIND_NON_ZERO
		}

		override fun isDone(): Boolean {
			return index >= shape.pointsCount
		}

		override fun next() {
			index++
		}
	}
}