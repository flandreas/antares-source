package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.base.logger

/**
 * Represents the supported strategies for layouting the segments of an [EdgeView].
 */
enum class Layout(val customName: String, val inputEventHandler: EdgeViewInputEventHandler) {

    STRAIGHT("straight", EdgeViewInputEventHandler()) {
        override fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
            val layout = StraightEdgeViewLayout()
            return layout.layout(edgeView, graphView, begin, end)
        }

        override fun getSegmentDirection(edgeView: EdgeView<*>, segmentIndex: Int): Direction? {
            return null
        }
    },

    ORTHOGONAL("ortho", DragEdgeSegmentHandler()) {
        override fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
            val layout = OrthoEdgeViewLayout()
            return layout.layout(edgeView, graphView, begin, end)
        }
    },

    NONE("none", DragEdgeSegmentHandler()) {
        override fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D> {
            val layout = NoneEdgeViewLayout()
            return layout.layout(edgeView, graphView, begin, end)
        }
    };

    companion object {

        val LOG by logger()

        fun withName(customName: String): Layout {
            for (i in 0..Layout.values().size - 1) {
                if (values()[i].customName == customName) {
                    return values()[i]
                }
            }
            LOG.error("Unknown Layout $customName")
            throw IllegalArgumentException("Unknown Layout " + customName)
        }
    }

    abstract fun layout(edgeView: EdgeView<*>, graphView: GraphView<*>, begin: LayoutBoundary, end: LayoutBoundary): List<Point2D>

    override fun toString(): String {
        return when (this) {
            STRAIGHT -> Translations.getString("graph.property.edgeView.layout.straight.name")
            ORTHOGONAL -> Translations.getString("graph.property.edgeView.layout.orthogonal.name")
            NONE -> Translations.getString("graph.property.edgeView.layout.none.name")
        }
    }

    open fun getSegmentDirection(edgeView: EdgeView<*>, segmentIndex: Int): Direction? {
        if (edgeView.isDegenerated) {
            // LOG.warn("EdgeView is degenerated at index {}", segmentIndex);
            return null
        }
        if (!edgeView.isSegmentOrthogonal(segmentIndex)) {
            // LOG.warn("EdgeView is not orthogonal at segment {}. Cannot determine Direction.", segmentIndex);
            return null
        }

        return Direction.of(
            Point2D(edgeView.getSegmentPoint(segmentIndex)),
            Point2D(edgeView.getSegmentPoint(segmentIndex + 1)))

    }
}

/** Represents a boundary of a region of an [EdgeView] that is to be layouted. */
data class LayoutBoundary(val point: Point2D, val directions: Set<Direction>, val isPort: Boolean)