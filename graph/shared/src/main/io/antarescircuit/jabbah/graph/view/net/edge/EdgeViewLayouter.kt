package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.base.geom.Point2D

/**
 * Creates a [Point2D] layout for the segments of an [EdgeView].
 */
interface EdgeViewLayouter {

	fun layoutOrigin(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, destPointIndex: Int, compact: Boolean)

	fun layoutDestination(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary, origPointIndex: Int, compact: Boolean)

	fun layoutAll(edgeView: EdgeView<*>, graphView: GraphView, begin: LayoutBoundary, end: LayoutBoundary)
}