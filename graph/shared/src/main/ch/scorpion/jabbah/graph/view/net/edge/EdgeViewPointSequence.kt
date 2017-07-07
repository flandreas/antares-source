package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.animation.CompositeSequence
import ch.scorpion.jabbah.animation.PointRange
import ch.scorpion.jabbah.animation.Sequence
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Represents the [Sequence] of the [Point2D]s of which an [EdgeView] consists.
 */
class EdgeViewPointSequence private constructor(
    edgeView: EdgeView<*>,
    isReverse: Boolean,
    vararg sequences: Sequence<Point2D>
) : CompositeSequence<Point2D>(*sequences) {

    companion object {

        fun of(edgeView: EdgeView<*>): EdgeViewPointSequence {
            val list = mutableListOf<Sequence<Point2D>>()
            for (i in 0..edgeView.segmentPointCount - 2) {
                list.add(PointRange(edgeView.getSegmentPoint(i), edgeView.getSegmentPoint(i + 1)))
            }
            return EdgeViewPointSequence(edgeView, false, *list.toTypedArray())
        }

        fun reverseOf(edgeView: EdgeView<*>): EdgeViewPointSequence {
            val list = mutableListOf<Sequence<Point2D>>()
            for (i in edgeView.segmentPointCount - 1 downTo 1) {
                list.add(PointRange(edgeView.getSegmentPoint(i), edgeView.getSegmentPoint(i - 1)))
            }
            return EdgeViewPointSequence(edgeView, true, *list.toTypedArray())
        }
    }

    private val length: Double = edgeView.calculateMaximumNetLength(isReverse)

    override val size: Double get() = length
}