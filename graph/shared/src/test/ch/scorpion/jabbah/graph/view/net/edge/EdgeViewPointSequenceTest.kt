package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [EdgeViewPointSequence].
 */
class EdgeViewPointSequenceTest {

    companion object {
	    init {
		    GraphViewTestRule.configure()
	    }
    }

    @Test
    fun shouldBuildSequence() {
        val edgeView = GraphViewModule.getEdgeViewFactory<Boolean>().createEdgeView()

        edgeView.addSegmentPoint(Point2D(0, 0))
        edgeView.addSegmentPoint(Point2D(10, 0))
        edgeView.addSegmentPoint(Point2D(10, 10))

        val sequence = EdgeViewPointSequence.of(edgeView)

        assertEquals(sequence.size, (20.0))

        assertEquals(Point2D(0, 0), sequence.getNext(10.0))
        assertEquals(Point2D(10, 0), sequence.getNext(10.0))
        assertEquals(Point2D(10, 0), sequence.getNext(10.0))
        assertEquals(Point2D(10, 10), sequence.getNext(10.0))
    }

    @Test
    fun shouldBuildReverseSequence() {
        val edgeView = GraphViewModule.getEdgeViewFactory<Boolean>().createEdgeView()

        edgeView.addSegmentPoint(Point2D(0, 0))
        edgeView.addSegmentPoint(Point2D(10, 0))
        edgeView.addSegmentPoint(Point2D(10, 10))

        val sequence = EdgeViewPointSequence.reverseOf(edgeView)

        assertEquals(20.0, sequence.size)
        assertEquals(Point2D(10, 10), sequence.getNext(10.0))
		assertEquals(Point2D(10, 0), sequence.getNext(10.0))
		assertEquals(Point2D(10, 0), sequence.getNext(10.0))
		assertEquals(Point2D(0, 0), sequence.getNext(10.0))
    }
}