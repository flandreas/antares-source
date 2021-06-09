package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * The layout algorithm for [LayoutType.NONE].
 */
object NoneEdgeViewLayouter : EdgeViewLayouter {

	override fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean) {
		val points = mutableListOf<Point2D>()
		points.add(begin.point)
		if (edgeView.polyline.pointsCount > 1) {
			points.addAll(edgeView.polyline.getPoints(1, edgeView.polyline.pointsCount))
		}
		edgeView.setLaidOutPoints(points, compact)
	}

	override fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean) {
		val points = mutableListOf<Point2D>()
		if (edgeView.polyline.pointsCount > 1) {
			points.addAll(edgeView.polyline.getPoints(0, edgeView.polyline.pointsCount - 1))
		}
		points.add(end.point)
		edgeView.setLaidOutPoints(points, compact)
	}

	override fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary) {
		val points = mutableListOf<Point2D>()
		points.add(begin.point)
		if (edgeView.polyline.pointsCount > 2) {
			points.addAll(edgeView.polyline.getPoints(1, edgeView.polyline.pointsCount - 1))
		}
		points.add(end.point)
		edgeView.setLaidOutPoints(points, true)
	}
}