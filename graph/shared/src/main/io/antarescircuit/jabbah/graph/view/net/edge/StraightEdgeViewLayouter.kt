package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * The layout algorithm for [LayoutType.STRAIGHT].
 */
object StraightEdgeViewLayouter : EdgeViewLayouter {

	override fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean) {
		edgeView.setLaidOutPoints(listOf(begin.point, end.point), false)
	}

	override fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean) {
		edgeView.setLaidOutPoints(listOf(begin.point, end.point), false)
	}

	override fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary) {
		edgeView.setLaidOutPoints(listOf(begin.point, end.point), false)
	}
}