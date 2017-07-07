package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit tests for [EdgeViewPointSequence].
 */
class EdgeViewPointSequenceTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    @Test
    fun shouldBuildSequence() {
        val edgeView = GraphViewModule.getEdgeViewFactory<Boolean>().createEdgeView()

        edgeView.addSegmentPoint(Point2D(0, 0))
        edgeView.addSegmentPoint(Point2D(10, 0))
        edgeView.addSegmentPoint(Point2D(10, 10))

        val sequence = EdgeViewPointSequence.of(edgeView)

        assertThat(sequence.size, `is`(20.0))

        assertThat(sequence.getNext(10.0), `is`(Point2D(0, 0)))
        assertThat(sequence.getNext(10.0), `is`(Point2D(10, 0)))
        assertThat(sequence.getNext(10.0), `is`(Point2D(10, 0)))
        assertThat(sequence.getNext(10.0), `is`(Point2D(10, 10)))
    }

    @Test
    fun shouldBuildReverseSequence() {
        val edgeView = GraphViewModule.getEdgeViewFactory<Boolean>().createEdgeView()

        edgeView.addSegmentPoint(Point2D(0, 0))
        edgeView.addSegmentPoint(Point2D(10, 0))
        edgeView.addSegmentPoint(Point2D(10, 10))

        val sequence = EdgeViewPointSequence.reverseOf(edgeView)

        assertThat(sequence.size, `is`(20.0))
        assertThat(sequence.getNext(10.0), `is`(Point2D(10, 10)))
		assertThat(sequence.getNext(10.0), `is`(Point2D(10, 0)))
		assertThat(sequence.getNext(10.0), `is`(Point2D(10, 0)))
		assertThat(sequence.getNext(10.0), `is`(Point2D(0, 0)))
    }
}