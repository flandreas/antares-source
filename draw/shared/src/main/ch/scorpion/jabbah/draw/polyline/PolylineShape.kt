package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.collection.indexOfFirstOrNull
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.geom.Point2D.Companion.xRange
import ch.scorpion.jabbah.base.geom.Point2D.Companion.yRange
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A [Polyline] implementation that can be rendered as a shape by the [Graphics2D] engine.
 */
interface PolylineShape : Polyline, Shape {

	companion object {

		fun isSegmentHorizontal(index: Int, points: List<Point2D>): Boolean =
			Geometry.equal(points[index].y, points[index + 1].y)

		fun isSegmentVertical(index: Int, points: List<Point2D>): Boolean =
			Geometry.equal(points[index].x, points[index + 1].x)

		fun calculateInterference(points: List<Point2D>, others: List<List<Point2D>>): PolylineInterference =
			PolylineInterference(
				others.sumOf { calculateIntersectionCount(points, it) },
				others.sumOf { calculateOverlappingCount(points, it) }
			)

		private fun calculateIntersectionCount(points: List<Point2D>, other: List<Point2D>): Int =
			(0 until points.size - 1).sumOf { calculateIntersectionCount(it, points, other) }

		private fun calculateIntersectionCount(index: Int, points: List<Point2D>, other: List<Point2D>): Int =
			(0 until other.size - 1).count { doSegmentsIntersect(index, points, it, other) }

		private fun doSegmentsIntersect(index: Int, points: List<Point2D>, otherIndex: Int, other: List<Point2D>): Boolean {
			return if (isSegmentHorizontal(index, points) && isSegmentVertical(otherIndex, other)) {
				xRange(points[index], points[index + 1]).contains(other[otherIndex].x)
					&& yRange(other[otherIndex], other[otherIndex + 1]).contains(points[index].y)
			} else if (isSegmentVertical(index, points) && isSegmentHorizontal(otherIndex, other)) {
				yRange(points[index], points[index + 1]).contains(other[otherIndex].y)
					&& xRange(other[otherIndex], other[otherIndex + 1]).contains(points[index].x)
			} else {
				false
			}
		}

		private fun calculateOverlappingCount(points: List<Point2D>, other: List<Point2D>): Int =
			(0 until points.size - 1).sumOf { calculateOverlappingCount(points, it, other) }

		private fun calculateOverlappingCount(points: List<Point2D>, index: Int, other: List<Point2D>): Int =
			(0 until other.size - 1).count { overlapsOrthogonally(index, points, other, it) }

		fun overlapsOrthogonally(index: Int, points: List<Point2D>, other: List<Point2D>, otherIndex: Int): Boolean =
			if (isSegmentHorizontal(index, points) && isSegmentHorizontal(otherIndex, other)) {
				overlapHorizontally(index, points, other, otherIndex)
			} else if (isSegmentVertical(index, points) && isSegmentVertical(otherIndex, other)) {
				overlapVertically(points, index, other, otherIndex)
			} else {
				false
			}

		private fun overlapHorizontally(index: Int, points: List<Point2D>, other: List<Point2D>, otherIndex: Int): Boolean =
			abs(points[index].y - other[otherIndex].y) <= Polyline.OVERLAPS_SIZE
				&& (points[index].x >= other[otherIndex].x && points[index].x <= other[otherIndex + 1].x
				|| points[index + 1].x >= other[otherIndex].x && points[index + 1].x <= other[otherIndex + 1].x)

		private fun overlapVertically(points: List<Point2D>, index: Int, other: List<Point2D>, otherIndex: Int): Boolean {
			if (abs(points[index].x - other[otherIndex].x) > Polyline.OVERLAPS_SIZE) {
				return false
			}

			val range = (min(points[index].y, points[index + 1].y) .. max(points[index].y, points[index + 1].y))
			return range.contains(other[otherIndex].y) || range.contains(other[otherIndex + 1].y)
		}
	}
}

expect object PolylineShapeFactory {

	/** Creates a [PolylineShape] for the specified [Point2D]s.*/
	fun create(points: List<Point2D>?): PolylineShape
}

/**
 * An implementation of the [PolylineShape] interface
 * @property points the points of which this [Polyline] consists
 */
class PolylineShapeImpl(pts: List<Point2D>? = mutableListOf()) : PolylineShape {

	constructor(vararg pts: Point2D): this(pts.toList())

	companion object {

		/** Half of the size of the rectangle that is used for checking the containedness of a point.*/
		const val CONTAINS_SENSITIVITY = 2.0
	}

	private val points = mutableListOf<Point2D>()

	private val segmentIndices: IntRange get() = 0 until pointsCount - 1

	private var _boundingBox = Rectangle2D()

	init {
		if (pts != null) {
			points.addAll(pts)
		}
		updateBoundingBox()
	}

	override fun getPointList(): List<Point2D> = points

	/** ---- [Shape] interface */

	override val boundingBox: RectangularShape get() = _boundingBox

	private fun updateBoundingBox() {
		val bbox = Rectangle2D()
		if (pointsCount == 0) {
			_boundingBox = bbox
			return
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

		_boundingBox = bbox
	}

	override fun contains(x: Double, y: Double): Boolean = containsInArea(x, y)
		|| intersects(
		x - CONTAINS_SENSITIVITY,
		y - CONTAINS_SENSITIVITY,
		2 * CONTAINS_SENSITIVITY,
		2 * CONTAINS_SENSITIVITY)

	override fun contains(x: Double, y: Double, width: Double, height: Double): Boolean = false

	override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean =
		intersects(Rectangle2D(x, y, w, h))

	/** ---- [Polyline] interface */

	override val length: Double
		get() {
			var l = 0.0
			if (pointsCount > 1) {
				for (i in segmentIndices) {
					l += getSegmentLength(i)
				}
			}
			return l
		}

	override val pointsCount: Int get() = points.size

	override var beginLineTerminator: LineTerminator? = null
		set(value) {
			field = value
			updateLineTerminatorLocations()
			updateBoundingBox()
		}

	override var endLineTerminator: LineTerminator? = null
		set(value) {
			field = value
			updateLineTerminatorLocations()
			updateBoundingBox()
		}

	override fun clear() {
		points.clear()
		updateBoundingBox()
	}

	override fun addPoint(x: Double, y: Double): Polyline {
		points.add(Point2D(x, y))
		updateLineTerminatorLocations()
		updateBoundingBox()
		return this
	}

	override fun addPointAt(index: Int, x: Double, y: Double): Polyline {
		points.add(index, Point2D(x, y))
		updateLineTerminatorLocations()
		updateBoundingBox()
		return this
	}

	override fun removePoint(index: Int): Polyline {
		points.removeAt(index)
		updateLineTerminatorLocations()
		updateBoundingBox()
		return this
	}

	override fun getPointAt(index: Int): Point2D = points[index]

	override fun setPointAt(index: Int, x: Double, y: Double): Polyline {
		points[index] = Point2D(x, y)
		updateLineTerminatorLocations()
		updateBoundingBox()
		return this
	}

	override fun setPoints(points: List<Point2D>): Polyline {
		this.points.clear()
		for (p in points) {
			this.points.add(Point2D(p))
		}
		updateLineTerminatorLocations()
		updateBoundingBox()
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
		updateBoundingBox()
		return this
	}

	/** Returns always 0, because [PolylineShape] is pure geometry and doesn't have a notion of a stroke width.*/
	override fun getLineWidth(): Double = 0.0

	override fun findSegment(x: Double, y: Double, area: Int): Int? {
		val rect = Rectangle2D(x - area, y - area, 2 * area.toDouble(), 2 * area.toDouble())
		return segmentIndices.firstOrNull { intersectsSegment(it, rect) }
	}

	override fun findPoint(x: Double, y: Double, area: Int): Int? =
		points.indexOfFirstOrNull { it.isNear(x, y, area) }

	override fun getCenterOfSegment(index: Int): Point2D =
		Point2D(
			points[index].x + (points[index + 1].x - points[index].x) / 2,
			points[index].y + (points[index + 1].y - points[index].y) / 2)

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
		if (changed) {
			updateBoundingBox()
		}
		return changed
	}

	override fun getSegmentLength(index: Int): Double =
		points[index].distance(points[index + 1])

	override fun getPoints(startIndex: Int, endIndex: Int): List<Point2D> =
		points.subList(startIndex, endIndex)

	override fun isSegmentHorizontal(index: Int): Boolean = PolylineShape.isSegmentHorizontal(index, points)

	override fun isSegmentVertical(index: Int): Boolean = PolylineShape.isSegmentVertical(index, points)

	override fun isSegmentOrthogonalTo(index: Int, otherIndex: Int, other: List<Point2D>): Boolean =
		PolylineShape.isSegmentHorizontal(index, points) && PolylineShape.isSegmentVertical(otherIndex, other)
			|| PolylineShape.isSegmentVertical(index, points) && PolylineShape.isSegmentHorizontal(otherIndex, other)

	override fun mirrorHorizontally(x: Double) {
		setPoints(points.map { it.mirrorHorizontally(x) })
	}

	override fun mirrorVertically(y: Double) {
		setPoints(points.map { it.mirrorVertically(y) })
	}

	override fun reverse() {
		val buffer = points.toMutableList().asReversed()
		points.clear()
		points.addAll(buffer)
		updateLineTerminatorLocations()
		updateBoundingBox()
	}

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		setPoints(points.map { direction.rotation.rotatePointAround(pivot ?: getFirstPoint(), it) })
	}

	override fun overlapsOrthogonallyWith(otherIndex: Int, other: List<Point2D>): Boolean =
		segmentIndices.any { PolylineShape.overlapsOrthogonally(it, points, other, otherIndex) }

	/** ---- [PolylineShape]  */

	private fun updateLineTerminatorLocations() {
		if (beginLineTerminator != null && pointsCount >= 2) {
			beginLineTerminator!!.setLocation(points[0], points[1])
		}
		if (endLineTerminator != null && pointsCount >= 2) {
			endLineTerminator!!.setLocation(points.last(), points[pointsCount - 2])
		}
	}

	private fun intersects(r: Rectangle2D): Boolean =
		segmentIndices.any { intersectsSegment(it, r) }

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
	private fun isCoparallelX(index: Int): Boolean =
		Geometry.equal(points[index].x, points[index - 1].x) && Geometry.equal(points[index].x, points[index + 1].x)

	/** Checks whether the point at the specified index has the same y coordinate as its two neighbours.*/
	private fun isCoparallelY(index: Int): Boolean =
		Geometry.equal(points[index].y, points[index - 1].y) && Geometry.equal(points[index].y, points[index + 1].y)

	// From java.awt.Polygon.contains(double, double)
	private fun containsInArea(x: Double, y: Double): Boolean {
		if (points.size <= 2 || !boundingBox.contains(x, y)) {
			return false
		}
		var hits = 0
		var lastX: Double
		var lastY: Double
		var curX = points.last().x
		var curY = points.last().y


		var i = -1
		while (i < points.size - 1) {
			i++
			lastX = curX
			lastY = curY

			curX = points[i].x
			curY = points[i].y

			if (curY == lastY) {
				continue
			}

			var leftX: Int = if (curX < lastX) {
				if (x >= lastX) {
					continue
				}
				curX.toInt()
			} else {
				if (x >= curX) {
					continue
				}
				lastX.toInt()
			}

			var test1: Double
			var test2: Double
			if (curY < lastY) {
				if (y < curY || y >= lastY) {
					continue
				}
				if (x < leftX) {
					hits++
					continue
				}
				test1 = x - curX
				test2 = y - curY
			} else {
				if (y < lastY || y >= curY) {
					continue
				}
				if (x < leftX) {
					hits++
					continue
				}
				test1 = x - lastX
				test2 = y - lastY
			}

			if (test1 < test2 / (lastY - curY) * (lastX - curX)) {
				hits++
			}
		}

		return hits.and(1) != 0
	}
}