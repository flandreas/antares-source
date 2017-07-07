package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * The layout algorithm for [Layout.NONE].
 */
class NoneEdgeViewLayout : EdgeViewLayout {

    override fun layout(edgeView: EdgeView<*>?, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
        val list = mutableListOf<Point2D>()
        edgeView?.let {
            for (i in 0..edgeView.segmentPointCount - 1) {
                list.add(edgeView.getSegmentPoint(i))
            }
            list[0] = begin.point
            list[list.size - 1] = end.point
        }
        return list
    }
}