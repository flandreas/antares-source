package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Creates a layout for the segment of a {@link EdgeView}.
 */
interface EdgeViewLayout {

    /**
     * Returns a [List] of [Point2D]s that represent an orthogonal segment layout between two points.
     * @return the resulting [Point2D]s
     */
    fun layout(edgeView: EdgeView<*>?, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D>

}