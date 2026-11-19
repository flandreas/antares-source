package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.animation.Sequence
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.math.SIGMA
import io.antarescircuit.jabbah.graph.view.EdgeView

/**
 * A cursor along an [EdgeView], retaining primitive state between animation steps.
 * Segment vertices are read without copying; length and direction are calculated only when entering
 * a segment. Endpoints are returned explicitly when [returnSequenceEndPoint] is true.
 *
 * @param size the maximum downstream net length used for animation timing. Callers traversing a net
 * can supply a cached value instead of recalculating it for each edge animation.
 */
class EdgeViewPointSequence(
	private val edgeView: EdgeView<*>,
	private val isReverse: Boolean,
	private val returnSequenceEndPoint: Boolean,
	offset: Double,
	override val size: Double,
) : Sequence<Point2D> {

	constructor(
		edgeView: EdgeView<*>,
		isReverse: Boolean = false,
		returnSequenceEndPoint: Boolean = edgeView.segmentPointCount == 2,
		offset: Double = 0.0,
	) : this(edgeView, isReverse, returnSequenceEndPoint, offset, edgeView.calculateMaximumNetLength(isReverse))

	private var segmentPointIndex = if (isReverse) edgeView.segmentPointCount - 1 else 0
	private var begin = Point2D.ZERO
	private var end = Point2D.ZERO
	private var segmentLength = 0.0
	private var directionX = 0.0
	private var directionY = 0.0
	private var segmentDistance = 0.0
	private var currentX = 0.0
	private var currentY = 0.0
	private var hasCurrent = false

	var remainder: Double = 0.0
		private set

	init {
		if (edgeView.segmentPointCount > 0) {
			enterSegment(offset)
		}
	}

	override fun getCurrent(): Point2D? = if (hasCurrent) currentPoint() else null

	override fun getNext(distance: Double): Point2D? {
		if (!ensureCurrent()) {
			return null
		}
		val point = currentPoint()
		advance(distance)
		return point
	}

	/** Consumes the remaining coordinates without constructing [Point2D]s. */
	fun forEach(distance: Double, handler: (x: Double, y: Double) -> Unit) {
		while (ensureCurrent()) {
			val x = currentX
			val y = currentY
			advance(distance)
			handler(x, y)
		}
	}

	private fun currentPoint(): Point2D = when {
		segmentDistance == 0.0 -> begin
		segmentDistance == segmentLength -> end
		else -> Point2D(currentX, currentY)
	}

	private fun ensureCurrent(): Boolean {
		while (!hasCurrent && hasNextSegment()) {
			segmentPointIndex += if (isReverse) -1 else 1
			enterSegment(remainder)
		}
		return hasCurrent
	}

	private fun hasNextSegment(): Boolean = if (isReverse) {
		segmentPointIndex > 1
	} else {
		segmentPointIndex < edgeView.segmentPointCount - 2
	}

	private fun enterSegment(offset: Double) {
		val shape = edgeView.polyline
		begin = shape.getPointAt(segmentPointIndex)
		end = if (shape.pointsCount == 1) begin else shape.getPointAt(segmentPointIndex + if (isReverse) -1 else 1)
		segmentLength = begin.distance(end)
		directionX = if (segmentLength > SIGMA) (end.x - begin.x) / segmentLength else 0.0
		directionY = if (segmentLength > SIGMA) (end.y - begin.y) / segmentLength else 0.0
		segmentDistance = 0.0
		currentX = begin.x
		currentY = begin.y
		hasCurrent = true
		remainder = 0.0
		if (offset > SIGMA) {
			advance(offset)
		}
	}

	private fun advance(distance: Double) {
		if (segmentLength <= SIGMA) {
			hasCurrent = false
			return
		}

		var nextDistance = segmentDistance + if (distance <= 0.0) SIGMA else distance
		if (nextDistance >= segmentLength) {
			// Advancing from an explicitly visited endpoint records the outgoing step for
			// the next segment/edge, even though this segment is now exhausted.
			remainder = nextDistance - segmentLength
			if (nextDistance == segmentLength || returnSequenceEndPoint) {
				nextDistance = segmentLength
			} else {
				hasCurrent = false
				return
			}
		}

		val nextX = if (nextDistance == segmentLength) end.x else begin.x + directionX * nextDistance
		val nextY = if (nextDistance == segmentLength) end.y else begin.y + directionY * nextDistance
		hasCurrent = nextX != currentX || nextY != currentY
		segmentDistance = nextDistance
		currentX = nextX
		currentY = nextY
	}
}
