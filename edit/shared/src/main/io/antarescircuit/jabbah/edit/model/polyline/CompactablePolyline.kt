package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.base.geom.*
import kotlin.math.PI
import kotlin.math.abs

/**
 * An [CompactablePolyline] is a sequence of [Point2D]s that keeps only those points that form a sequence of
 * collinear segments.
 */
class CompactablePolyline(points: List<Point2D>) {

	constructor() : this(listOf())

	companion object {
		/** The maximal angle between two line segment that leads to compaction. */
		const val COMPACT_ANGLE = 0.1
	}

	val points: List<Point2D> get() = _points

	val size: Int get() = _points.size

	val boundingBox: RectangularShape get() {
		if (boundingBoxDirty) {
			updateBoundingBox()
		}
		return Rectangle2D(_boundingBox)
	}

	val isDegenerated: Boolean get() = size < 2 || _points[0] == _points[1]

	val isOrthogonal: Boolean get() = !isDegenerated && (0 until size - 1).all { isSegmentOrthogonal(it) }

	private val _points = mutableListOf<Point2D>()

	private val _boundingBox = Rectangle2D()
	private var boundingBoxDirty = false

	/** Earlier triples are unchanged by an append and were already checked. */
	private var nextCompactionIndex = 1

	init {
		add(points)
	}

	/** ---- [Any] */

	override fun toString(): String {
		val s = StringBuilder()
		_points.forEach { s.append("(${it.x},${it.y})") }
		return s.toString()
	}

	/** ---- [CompactablePolyline] */

	fun add(point: Point2D): CompactablePolyline {
		if (_points.isEmpty()) {
			_boundingBox.setFrame(point.x, point.y, 1.0, 1.0)
		} else if (!boundingBoxDirty) {
			_boundingBox.add(point)
		}
		_points.add(point)
		compactFrom(nextCompactionIndex)
		return this
	}

	fun add(points: List<Point2D>): CompactablePolyline {
		if (points.isNotEmpty()) {
			_points.addAll(points)
			boundingBoxDirty = true
			compact()
		}
		return this
	}

	fun get(index: Int): Point2D = _points[index]

	fun getSegmentDirection(segmentIndex: Int): Direction? {
		if (isDegenerated) {
			return null
		}
		return Direction.optionalOf(_points[segmentIndex], _points[segmentIndex + 1])
	}

	/** Removes all intermediate points that lie between two collinear segments */
	fun compact() {
		compactFrom(1)
	}

	private fun compactFrom(startIndex: Int) {
		if (_points.size <= 2) {
			nextCompactionIndex = 1
			return
		}
		var i = startIndex
		var recheckFrom = Int.MAX_VALUE
		while (i < _points.size) {
			if (canCompactPoint(i)) {
				if (!boundingBoxDirty) {
					checkRemovedExtremum(i)
				}
				_points.removeAt(i)
				// The preceding triple may change after removal. Like a full forward scan,
				// defer checking it until the next append or explicit compact().
				recheckFrom = minOf(recheckFrom, (i - 1).coerceAtLeast(1))
			} else {
				i++
			}
		}
		nextCompactionIndex = minOf(recheckFrom, _points.lastIndex.coerceAtLeast(1))
	}

	private fun checkRemovedExtremum(index: Int) {
		val point = _points[index]
		val previous = _points[index - 1]
		val next = _points.getOrNull(index + 1) ?: previous
		if ((Geometry.equal(point.x, _boundingBox.x) && previous.x > point.x && next.x > point.x)
			|| (Geometry.equal(point.x, _boundingBox.maxX) && previous.x < point.x && next.x < point.x)
			|| (Geometry.equal(point.y, _boundingBox.y) && previous.y > point.y && next.y > point.y)
			|| (Geometry.equal(point.y, _boundingBox.maxY) && previous.y < point.y && next.y < point.y)) {
			boundingBoxDirty = true
		}
	}

	/** Updates the bounding box according to the current points. */
	private fun updateBoundingBox() {
		if (_points.size > 0) {
			// Height and width of zero would make the Rectangle2D 'initial', which is wrong if first point is (0,0)
			_boundingBox.setFrame(_points[0].x, _points[0].y, 1.0, 1.0)
		}
		_points.forEach { _boundingBox.add(it) }
		_boundingBox.setFrame(
			_boundingBox.x, _boundingBox.y,
			_boundingBox.width, _boundingBox.height)
		boundingBoxDirty = false
	}

	/**
	 * Checks whether the [Point2D] at a specific index can be compacted because it is an intermediate point of two
	 * collinear segments.
	 * @param i the index of the [Point2D] to check
	 * @return `true` if the [Point2D] at index `i` can be removed.
	 */
	private fun canCompactPoint(i: Int): Boolean {
		if (_points[i] == _points[i - 1]) {
			return true
		}
		if (i > _points.size - 2) {
			return false
		}

		val previous = _points[i - 1]
		val point = _points[i]
		val next = _points[i + 1]
		val angle1 = Geometry.angle(0.0, 0.0, previous.y - point.y, point.x - previous.x)
		val angle2 = Geometry.angle(0.0, 0.0, point.y - next.y, next.x - point.x)

		return abs(angle1 - angle2) <= COMPACT_ANGLE || abs(abs(angle1 - angle2) - PI) < COMPACT_ANGLE
	}

	private fun isSegmentHorizontal(index: Int): Boolean =
		Geometry.equal(_points[index].y, _points[index + 1].y)

	private fun isSegmentVertical(index: Int): Boolean =
		Geometry.equal(_points[index].x, _points[index + 1].x)

	private fun isSegmentOrthogonal(index: Int): Boolean =
		isSegmentHorizontal(index) || isSegmentVertical(index)
}
