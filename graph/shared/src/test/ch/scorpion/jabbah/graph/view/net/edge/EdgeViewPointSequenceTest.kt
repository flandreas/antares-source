package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.mockk
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
        val sequence = EdgeViewPointSequence(
	        GraphViewModule.getEdgeViewFactory()
		        .createEdgeView<Boolean>(mockk())
		        .addSegmentPoint(Point2D(0, 0))
		        .addSegmentPoint(Point2D(10, 0))
		        .addSegmentPoint(Point2D(10, 10)))

        assertEquals(sequence.size, (20.0))

        assertEquals(Point2D(0, 0), sequence.getNext(10.0))
        assertEquals(Point2D(10, 0), sequence.getNext(10.0))
	    // Don't return the same value twice
        assertEquals(Point2D(10, 10), sequence.getNext(10.0))
    }

    @Test
    fun shouldBuildReverseSequence() {
	    val sequence = EdgeViewPointSequence(
		    GraphViewModule.getEdgeViewFactory()
		        .createEdgeView<Boolean>(mockk())
		        .addSegmentPoint(Point2D(0, 0))
		        .addSegmentPoint(Point2D(10, 0))
		        .addSegmentPoint(Point2D(10, 10)),
	        isReverse = true)

        assertEquals(20.0, sequence.size)
        assertEquals(Point2D(10, 10), sequence.getNext(10.0))
		assertEquals(Point2D(10, 0), sequence.getNext(10.0))
	    // Don't return the same value twice
		assertEquals(Point2D(0, 0), sequence.getNext(10.0))
    }

	@Test
	fun shouldNotLooseMomentumAtSegmentPoints() {
		val sequence = EdgeViewPointSequence(
			GraphViewModule.getEdgeViewFactory()
				.createEdgeView<Boolean>(mockk())
				.addSegmentPoint(Point2D(0, 0))
				.addSegmentPoint(Point2D(10, 0))
				.addSegmentPoint(Point2D(10, 10)))

		assertEquals(Point2D(0, 0), sequence.getNext(7.0))
		assertEquals(Point2D(7, 0), sequence.getNext(7.0))
		// Don't return end segment point, leave reminder to next segment
		assertEquals(Point2D(10, 4), sequence.getNext(7.0))
		// Add remainder from previous segment
		assertEquals(Point2D(10, 10), sequence.getNext(7.0))
	}
}