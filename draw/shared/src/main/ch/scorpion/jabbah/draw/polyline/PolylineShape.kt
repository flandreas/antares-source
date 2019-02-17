package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Shape
import ch.scorpion.jabbah.draw.graphics.Graphics2D

/**
 * A [Polyline] implementation that can be rendered as a shape by the [Graphics2D] engine.
 */
interface PolylineShape : Polyline, Shape

/**
 * An implementation of the [PolylineShape] interface
 * @property points the points of which this [Polyline] consists
 */
class PolylineShapeImpl(pts: List<Point2D>? = mutableListOf()) : PolylineShape {

	companion object {
		/** Half of the size of the rectangle that is used for checking the containedness of a point.*/
		const val CONTAINS_SENSITIVITY = 2.0
	}

	private val points = mutableListOf<Point2D>()

	init {
		if (pts != null) {
			points.addAll(pts)
		}
	}

	/** ---- [Shape] interface */

	// TODO Shouldn't the bounding box be cached rather than calculating it each time?
	override val boundingBox: Rectangle2D
		get() {
			val bbox = Rectangle2D()
			if (pointsCount == 0) {
				return bbox
			}
			bbox.setFrame(points[0].x, points[0].y, 0.0, 0.0)
			for (i in 1 until pointsCount) {
				bbox.add(points[i])
			}
			if (beginLineTerminator != null) {
				bbox.add(beginLineTerminator!!.boundingBox)
			}
			if (endLineTerminator != null) {
				bbox.add(endLineTerminator!!.boundingBox)
			}

			return bbox
		}

	override fun contains(x: Double, y: Double): Boolean {
		return intersects(
			x - CONTAINS_SENSITIVITY,
			y - CONTAINS_SENSITIVITY,
			2 * CONTAINS_SENSITIVITY,
			2 * CONTAINS_SENSITIVITY
		)
	}

	override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean {
		return false
	}

	override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean {
		return intersects(Rectangle2D(x, y, w, h))
	}

	/** ---- [Polyline] interface */

	override val length: Double
		get() {
			var l = 0.0
			if (pointsCount > 1) {
				for (i in 0..pointsCount - 2) {
					l += getSegmentLength(i)
				}
			}
			return l
		}

	override val pointsCount: Int
		get() = points.size

	override var beginLineTerminator: LineTerminator? = null
		set(value) {
			field = value
			updateLineTerminatorLocations()
		}

	override var endLineTerminator: LineTerminator? = null
		set(value) {
			field = value
			updateLineTerminatorLocations()
		}

	override fun clear() {
		points.clear()
	}

	override fun addPoint(x: Double, y: Double): Polyline {
		points.add(Point2D(x, y))
		updateLineTerminatorLocations()
		return this
	}

	override fun addPointAt(index: Int, x: Double, y: Double): Polyline {
		points.add(index, Point2D(x, y))
		updateLineTerminatorLocations()
		return this
	}

	override fun removePoint(index: Int): Polyline {
		points.removeAt(index)
		updateLineTerminatorLocations()
		return this
	}

	override fun getPointAt(index: Int): Point2D {
		return points[index]
	}

	override fun setPointAt(index: Int, x: Double, y: Double): Polyline {
		points[index] = Point2D(x, y)
		updateLineTerminatorLocations()
		return this
	}

	override fun setPoints(points: List<Point2D>): Polyline {
		this.points.clear()
		for (p in points) {
			this.points.add(Point2D(p))
		}
		updateLineTerminatorLocations()
		return this
	}

	override fun setLocation(x: Double, y: Double): Polyline {
		if (pointsCount == 0) {
			return this
		}
		val dx = x - points[0].x
		val dy = y - points[0].y
		points.toList().forEachIndexed { index, p -> points[index] = Point2D(p.x + dx, p.y + dy) }
		updateLineTerminatorLocations()
		return this
	}

	/** Returns always 0, because [PolylineShape] is pure geometry and doesn't have a notion of a stroke width.*/
	override fun getLineWidth(): Double {
		return 0.0
	}

	override fun findSegment(x: Double, y: Double, area: Int): Int? {
		val rect = Rectangle2D(x - area, y - area, 2 * area.toDouble(), 2 * area.toDouble())
		return (0..pointsCount - 2).firstOrNull { intersectsSegment(it, rect) }
	}

	override fun findPoint(x: Double, y: Double, area: Int): Int? {
		return points.indexOfFirstOrNull { it.isNear(x, y, area) }
	}

	override fun getCenterOfSegment(index: Int): Point2D {
		return Point2D(
			points[index].x + (points[index + 1].x - points[index].x) / 2,
			points[index].y + (points[index + 1].y - points[index].y) / 2
		)
	}

	override fun compact(): Boolean {
		if (pointsCount <= 2) {
			return false
		}
		var i = 1
		var changed = false
		while (i < pointsCount) {
			if (canCompactPoint(i)) {
				changed = true
				points.removeAt(i)
			} else {
				i++
			}
		}
		return changed
	}

	override fun getSegmentLength(index: Int): Double {
		return points[index].distance(points[index + 1])
	}

	override fun getPoints(startIndex: Int, endIndex: Int): List<Point2D> {
		return points.subList(startIndex, endIndex)
	}

	override fun isSegmentOrthogonal(index: Int): Boolean {
		return points[index].x == points[index + 1].x || points[index].y == points[index + 1].y
	}

	override fun mirrorHorizontally(x: Double) {
		setPoints(points.map { it.mirrorHorizontally(x) })
	}

	override fun mirrorVertically(y: Double) {
		setPoints(points.map { it.mirrorVertically(y) })
	}

	/** ---- [PolylineShape]  */

	private fun updateLineTerminatorLocations() {
		if (beginLineTerminator != null && pointsCount >= 2) {
			beginLineTerminator!!.setLocation(points[0], points[1])
		}
		if (endLineTerminator != null && pointsCount >= 2) {
			endLineTerminator!!.setLocation(points.last(), points[pointsCount - 2])
		}
	}

	private fun intersects(r: Rectangle2D): Boolean {
		return (0..pointsCount - 2).any { intersectsSegment(it, r) }
	}

	/**
	 * Determines whether a segment of this [PolylineShape] intersects a given [Rectangle2D].
	 * @param index the index of the segment, where 0 references the first segment.
	 * @param r the [Rectangle2D] to check against.
	 * @return `true` if segment `index` intersects `r`.
	 */
	private fun intersectsSegment(index: Int, r: Rectangle2D): Boolean {
		if (pointsCount < 2) {
			return false
		}
		val start = points[index]
		val end = points[index + 1]
		return r.intersectsLine(start.x, start.y, end.x, end.y)
	}

	/**
	 * Checks whether the [Point2D] at a specific index can be compacted because it is an intermediate point of two
	 * coparallel segments, or because it is at the same location as its predecessor.
	 * @param index the index of the [Point2D] to check
	 * @return ´true´ if the [Point2D] at index ´index´ can be removed.
	 */
	private fun canCompactPoint(index: Int): Boolean {
		if (points[index] == points[index - 1]) {
			return true
		}
		if (index > pointsCount - 2) {
			return false
		}
		return isCoparallelX(index) || isCoparallelY(index)
	}

	/** Checks whether the point at the specified index has the same x coordinate as its two neighbours.*/
	private fun isCoparallelX(index: Int): Boolean {
		return points[index].x == points[index - 1].x && points[index].x == points[index + 1].x
	}

	/** Checks whether the point at the specified index has the same y coordinate as its two neighbours.*/
	private fun isCoparallelY(index: Int): Boolean {
		return points[index].y == points[index - 1].y && points[index].y == points[index + 1].y
	}
}