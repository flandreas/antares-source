package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.edit.SnapResult
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.EdgeViewSnapLocatorResult

//data class EdgeViewSnapLocatorResult(val segmentIndex: Int, val x: Double, val y: Double)

/** Snaps a point to an [EdgeView] with [LayoutType.ORTHOGONAL].*/
object EdgeViewSnapLocator {

	fun snap(edgeView: EdgeView<*>, x: Double, y: Double, backgroundSnapper: Snapper? = null): EdgeViewSnapLocatorResult? {

		val segmentIndex = edgeView.polyline.findSegment(x, y, EdgeView.containsSize) ?: return null

		// Try to snap to a nearby [EdgeView] corner, if any
		if (edgeView.getSegmentPoint(segmentIndex).distance(x, y) <= EdgeView.edgeCornerDistance) {
			return EdgeViewSnapLocatorResult(segmentIndex, edgeView.getSegmentPoint(segmentIndex).x, edgeView.getSegmentPoint(segmentIndex).y)
		}
		if (segmentIndex < edgeView.segmentPointCount - 2 && edgeView.getSegmentPoint(segmentIndex + 1).distance(x, y) <= EdgeView.edgeCornerDistance) {
			return EdgeViewSnapLocatorResult(segmentIndex, edgeView.getSegmentPoint(segmentIndex + 1).x, edgeView.getSegmentPoint(segmentIndex + 1).y)
		}

		val snap = SnapResult(x, y)
		backgroundSnapper?.snap(x, y, snap)
		if (edgeView.polyline.isSegmentHorizontal(segmentIndex)) {
			return EdgeViewSnapLocatorResult(segmentIndex, snap.x, edgeView.getSegmentPoint(segmentIndex).y)
		}
		if (edgeView.polyline.isSegmentVertical(segmentIndex)) {
			return EdgeViewSnapLocatorResult(segmentIndex, edgeView.getSegmentPoint(segmentIndex).x, snap.y)
		}

		return null
	}
}
