package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EdgeViewPointSequenceTest {

	init {
		GraphViewTestRule.configure()
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
		assertNull(sequence.getNext(7.0))
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
		assertNull(sequence.getNext(8.0))
	}

	@Test
	fun shouldDoForEach() {
		val sequence = createSequence(
			listOf(
				Point2D(0, 0),
				Point2D(10, 0),
				Point2D(10, 10)),
			returnSequenceEndpoints = false
		)
		assertForEachWithDistance(sequence, 8.0, listOf(
			Point2D(0, 0),
			Point2D(8, 0),
			Point2D(10, 6)))
	}

	@Test
	fun shouldDoForEachOnUShapedEdgeViewWithOffset() {
		val sequence = createSequence(
			listOf(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10), Point2D(0, 10)),
			returnSequenceEndpoints = false,
			offset = 5.0
		)

		assertForEachWithDistance(sequence, 8.0, listOf(
			Point2D(5.0, 0.0),
			Point2D(10.0, 3.0),
			Point2D(9.0, 10.0),
			Point2D(1.0, 10.0),
		))
	}

	/** Regression test for bug #583. */
	@Test
	fun shouldHandleDistanceLargerThanLastSegment() {
		val sequence = createSequence(
			listOf(
				Point2D(105, 77),
				Point2D(126, 77),
				Point2D(126, 49),
				Point2D(147, 49)
			),
			returnSequenceEndpoints = true,
			offset = 0.0
		)
		val distance = 50.0
		assertEquals(Point2D(105, 77), sequence.getNext(distance))
		assertEquals(Point2D(126, 77), sequence.getNext(distance))
		assertEquals(Point2D(126, 49), sequence.getNext(distance))
		assertEquals(Point2D(147, 49), sequence.getNext(distance))
		assertNull(sequence.getNext(distance))
	}

	private fun createSequence(
		points: List<Point2D>,
		reverse: Boolean = false,
		returnSequenceEndpoints: Boolean = points.size == 2,
		offset: Double = 0.0
	): EdgeViewPointSequence {
		val edgeView = GraphViewModule.getEdgeViewFactory()
			.createEdgeView<Boolean>(mock())
			.also { ev ->
				points.forEach { ev.addSegmentPoint(it) }
			}
		return EdgeViewPointSequence(edgeView, reverse, returnSequenceEndpoints, offset)
	}

	private fun createReverseSequence(points: List<Point2D>): EdgeViewPointSequence =
		createSequence(points, true)

	private fun assertForEachWithDistance(
		sequence: EdgeViewPointSequence,
		distance: Double,
		expected: List<Point2D>
	) {
		var index = 0
		sequence.forEach(distance) { x, y ->
			assertEquals(expected[index].x, x, "x of index $index:")
			assertEquals(expected[index].y, y, "y of index $index:")
			index++
		}
		assertEquals(expected.size, index)
	}
}