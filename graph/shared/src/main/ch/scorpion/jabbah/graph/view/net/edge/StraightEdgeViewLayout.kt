package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * The layout algorithm for [Layout.STRAIGHT].
 */
class StraightEdgeViewLayout : EdgeViewLayout {

    override fun layout(edgeView: EdgeView<*>?, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
        val list = mutableListOf<Point2D>()
        list.add(begin.point)
        list.add(end.point)
        return list
    }
}