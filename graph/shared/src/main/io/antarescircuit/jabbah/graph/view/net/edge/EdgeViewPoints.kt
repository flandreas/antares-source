package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.graph.view.EdgeView

/**
 * Visits coordinates spaced by [distance] along this edge, starting [offset] from its origin.
 * Carries spacing across bends and visits a shared vertex only once. The destination is visited
 * only if it falls on the spacing grid; zero-length segments do not add points.
 *
 * Reads existing vertices and uses primitive traversal state, without allocating a sequence,
 * ranges, or points. The handler is inlined to avoid boxing coordinates on JVM.
 * The handler must not modify the edge geometry during traversal.
 *
 * @param handler the code to be executed on the specified point on the [EdgeView],
 * for example drawing a dot at that point
 */
inline fun EdgeView<*>.forEachPoint(
	distance: Double,
	offset: Double = 0.0,
	handler: (x: Double, y: Double) -> Unit,
) {
	require(distance.isFinite() && distance > 0.0) { "Distance must be finite and positive" }
	require(offset.isFinite() && offset >= 0.0) { "Offset must be finite and non-negative" }

	val shape = polyline
	val pointCount = shape.pointsCount
	if (pointCount == 0) {
		return
	}

	var begin = shape.getPointAt(0)
	var nextDistance = offset
	if (nextDistance == 0.0) {
		handler(begin.x, begin.y)
		nextDistance = distance
	}

	for (index in 1 until pointCount) {
		val end = shape.getPointAt(index)
		val length = begin.distance(end)
		if (length > 0.0) {
			val directionX = (end.x - begin.x) / length
			val directionY = (end.y - begin.y) / length
			while (nextDistance <= length) {
				if (nextDistance == length) {
					handler(end.x, end.y)
				} else {
					handler(begin.x + directionX * nextDistance, begin.y + directionY * nextDistance)
				}
				val followingDistance = nextDistance + distance
				if (followingDistance <= nextDistance) {
					// Stop if floating-point precision cannot represent another step.
					return
				}
				nextDistance = followingDistance
			}
			nextDistance -= length
		}
		begin = end
	}
}
