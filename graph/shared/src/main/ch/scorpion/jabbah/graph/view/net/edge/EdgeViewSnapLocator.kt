package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.EdgeViewSnapLocatorResult

/** Snaps a point to an [EdgeView] with [LayoutType.ORTHOGONAL].*/
object EdgeViewSnapLocator {

	/** Must be larger than [EdgeView.edgeCornerDistance]. */
	const val FORBIDDEN_END_AREA = 16.0

	private val snappable = XYSnappable()

	fun snap(edgeView: EdgeView<*>, x: Double, y: Double, snapManager: SnapManager? = null): EdgeViewSnapLocatorResult? {
		snappable.set(x, y)

		val segmentIndex = edgeView.polyline.findSegment(x, y, EdgeView.containsSize) ?: return null

		if (isInForbiddenOriginArea(edgeView, x, y, segmentIndex)) {
			return null
		}

		if (isInForbiddenDestinationArea(edgeView, x, y, segmentIndex)) {
			return null
		}

		// Try to snap to a nearby [EdgeView] corner, if any
		if (edgeView.getSegmentPoint(segmentIndex).distance(x, y) <= EdgeView.edgeCornerDistance) {
			return EdgeViewSnapLocatorResult(segmentIndex, edgeView.getSegmentPoint(segmentIndex).x, edgeView.getSegmentPoint(segmentIndex).y)
		}
		if (segmentIndex < edgeView.segmentPointCount - 2 && edgeView.getSegmentPoint(segmentIndex + 1).distance(x, y) <= EdgeView.edgeCornerDistance) {
			return EdgeViewSnapLocatorResult(segmentIndex, edgeView.getSegmentPoint(segmentIndex + 1).x, edgeView.getSegmentPoint(segmentIndex + 1).y)
		}

		val snap = snapManager?.snap(snappable, 0.0, 0.0) ?: Point2D.ZERO

		if (edgeView.polyline.isSegmentHorizontal(segmentIndex)) {
			return EdgeViewSnapLocatorResult(segmentIndex, x + snap.x, edgeView.getSegmentPoint(segmentIndex).y)
		}
		if (edgeView.polyline.isSegmentVertical(segmentIndex)) {
			return EdgeViewSnapLocatorResult(segmentIndex, edgeView.getSegmentPoint(segmentIndex).x, y + snap.y)
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
