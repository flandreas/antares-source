package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
	    val sequence = createSequence(listOf(
		    Point2D(0, 0),
		    Point2D(10, 0),
		    Point2D(10, 10)))

        assertEquals(sequence.size, (20.0))

        assertEquals(Point2D(0, 0), sequence.getNext(10.0))
        assertEquals(Point2D(10, 0), sequence.getNext(10.0))
	    // Don't return the same value twice
        assertEquals(Point2D(10, 10), sequence.getNext(10.0))
    }

    @Test
    fun shouldBuildReverseSequence() {
	    val sequence = createReverseSequence(listOf(
		    Point2D(0, 0),
		    Point2D(10, 0),
		    Point2D(10, 10)))

        assertEquals(20.0, sequence.size)
        assertEquals(Point2D(10, 10), sequence.getNext(10.0))
		assertEquals(Point2D(10, 0), sequence.getNext(10.0))
	    // Don't return the same value twice
		assertEquals(Point2D(0, 0), sequence.getNext(10.0))
    }

	@Test
	fun shouldNotLooseMomentumAtSegmentPoints() {
		val sequence = createSequence(
			listOf(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10)),
			returnSequenceEndpoints = false
		)

		assertEquals(Point2D(0, 0), sequence.getNext(7.0))
		assertEquals(Point2D(7, 0), sequence.getNext(7.0))
		// Don't return end segment point, leave reminder to next segment
		assertEquals(Point2D(10, 4), sequence.getNext(7.0))
		assertFalse(sequence.hasNext())
	}

	@Test
	fun shouldNotReturnLastEndPoint() {
		val sequence = createSequence(
			listOf(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10)),
			returnSequenceEndpoints = false
		)

		assertEquals(Point2D(0, 0), sequence.getNext(8.0))
		assertEquals(Point2D(8, 0), sequence.getNext(8.0))
		assertEquals(Point2D(10, 6), sequence.getNext(8.0))
		assertFalse(sequence.hasNext())
	}

	@Test
	fun shouldDoForEach() {
		val sequence = createSequence(
			listOf(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10)),
			returnSequenceEndpoints = false
		)
		var index = 0
		val expected = listOf(Point2D(0, 0), Point2D(8, 0), Point2D(10, 6))

		sequence.forEach(8.0) { x, y ->
			assertEquals(expected[index].x, x)
			assertEquals(expected[index].y, y)
			index++
		}
		assertEquals(3, index)
	}

	private fun createSequence(
		points: List<Point2D>,
		reverse: Boolean = false,
		returnSequenceEndpoints: Boolean = points.size == 2
	): EdgeViewPointSequence {
		val edgeView = GraphViewModule.getEdgeViewFactory()
			.createEdgeView<Boolean>(mockk())
			.also { ev ->
				points.forEach { ev.addSegmentPoint(it) }
			}
		return EdgeViewPointSequence(edgeView, reverse, returnSequenceEndpoints)
	}

	private fun createReverseSequence(points: List<Point2D>): EdgeViewPointSequence =
		createSequence(points, true)
}