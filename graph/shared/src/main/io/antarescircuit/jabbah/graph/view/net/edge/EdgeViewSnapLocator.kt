package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.EdgeViewSnapLocatorResult

/** Snaps a point to an [EdgeView] with [LayoutType.ORTHOGONAL].*/
object EdgeViewSnapLocator {

	/** Must be larger than [EdgeView.EDGE_CORNER_DISTANCE]. */
	const val FORBIDDEN_END_AREA = 11.0

	private val snappable = XYSnappable()

	fun snap(edgeView: EdgeView<*>, x: Double, y: Double, outgoing: Boolean, snapManager: SnapManager? = null): EdgeViewSnapLocatorResult? {
		snappable.set(x, y)

		val segmentIndex = edgeView.polyline.findSegment(x, y, EdgeView.CONTAINS_SIZE) ?: return null

		if (isInForbiddenOriginArea(edgeView, x, y, segmentIndex)) {
			return null
		}

		if (isInForbiddenDestinationArea(edgeView, x, y, segmentIndex)) {
			return null
		}

		// Try to snap to a nearby [EdgeView] corner at the start of the segment, if any
		if (edgeView.getSegmentPoint(segmentIndex).distance(x, y) <= EdgeView.EDGE_CORNER_DISTANCE) {
			return EdgeViewSnapLocatorResult(
				segmentIndex,
				edgeView.getSegmentPoint(segmentIndex).x,
				edgeView.getSegmentPoint(segmentIndex).y,
				edgeView.getFreeCornerDirections(segmentIndex, outgoing)
			)
		}

		// Try to snap to a nearby [EdgeView] corner at the end of the segment, if any
		if (segmentIndex < edgeView.segmentPointCount - 2 && edgeView.getSegmentPoint(segmentIndex + 1).distance(x, y) <= EdgeView.EDGE_CORNER_DISTANCE) {
			return EdgeViewSnapLocatorResult(
				segmentIndex,
				edgeView.getSegmentPoint(segmentIndex + 1).x,
				edgeView.getSegmentPoint(segmentIndex + 1).y,
				edgeView.getFreeCornerDirections(segmentIndex + 1, outgoing)
			)
		}

		val snap = snapManager?.snap(snappable, 0.0, 0.0) ?: Point2D.ZERO

		if (edgeView.polyline.isSegmentHorizontal(segmentIndex)) {
			return EdgeViewSnapLocatorResult(
				segmentIndex,
				x + snap.x,
				edgeView.getSegmentPoint(segmentIndex).y,
				edgeView.getSegmentDirection(segmentIndex)?.orthogonalSet() ?: emptySet(),
			)
		}
		if (edgeView.polyline.isSegmentVertical(segmentIndex)) {
			return EdgeViewSnapLocatorResult(
				segmentIndex,
				edgeView.getSegmentPoint(segmentIndex).x,
				y + snap.y,
				edgeView.getSegmentDirection(segmentIndex)?.orthogonalSet() ?: emptySet())
		}

		return null
	}

	private fun isInForbiddenOriginArea(edgeView: EdgeView<*>, x: Double, y: Double, segmentIndex: Int): Boolean {
		return segmentIndex == 0 && edgeView.getSegmentPoint(segmentIndex).distance(x, y) <= FORBIDDEN_END_AREA
	}

	private fun isInForbiddenDestinationArea(edgeView: EdgeView<*>, x: Double, y: Double, segmentIndex: Int): Boolean {
		return segmentIndex == edgeView.segmentPointCount - 2 && edgeView.getSegmentPoint(segmentIndex + 1).distance(x, y) <= FORBIDDEN_END_AREA
	}

	private class XYSnappable : Snappable {
		override val snappableX: Array<SnappableX> = arrayOf(SnappableXCoordinate(0.0))
		override val snappableY: Array<SnappableY> = arrayOf(SnappableYCoordinate(0.0))

		fun set(x: Double, y: Double) {
			(snappableX[0] as SnappableXCoordinate).x = x
			(snappableY[0] as SnappableYCoordinate).y = y
		}

	}
}
