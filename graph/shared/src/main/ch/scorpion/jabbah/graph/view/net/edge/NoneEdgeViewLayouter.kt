package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * The layout algorithm for [LayoutType.NONE].
 */
object NoneEdgeViewLayouter : EdgeViewLayouter {

    override fun layout(edgeView: EdgeView<*>?, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
        val list = mutableListOf<Point2D>()
        edgeView?.let {
            for (i in 0 until edgeView.segmentPointCount) {
                list.add(edgeView.getSegmentPoint(i))
            }
            list[0] = begin.point
            list[list.size - 1] = end.point
        }
        return list
    }
}