package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

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
	fun shouldBuildSequenceWithOffset() {
		val sequence = createSequence(listOf(
			Point2D(0, 0),
			Point2D(10, 0),
			Point2D(10, 10)),
			offset = 2.0)

		assertEquals(Point2D(2, 0), sequence.getNext(5.0))
		assertEquals(Point2D(7, 0), sequence.getNext(5.0))
		assertEquals(Point2D(10, 2), sequence.getNext(5.0))
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
	fun shouldBuildReverseSequenceWithOffset() {
		val sequence = createReverseSequence(listOf(
			Point2D(0, 0),
			Point2D(10, 0),
			Point2D(10, 10)),
			offset = 2.0)

		assertEquals(Point2D(10, 8), sequence.getNext(5.0))
		assertEquals(Point2D(10, 3), sequence.getNext(5.0))
		assertEquals(Point2D(8, 0), sequence.getNext(5.0))
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

	/**
	 * Regression test for bug #1228.
	 * [EdgeViews][EdgeView] with only a single [Point2D] should not occur, as they are illegal.
	 * [EdgeViewPointSequence] must nonetheless be robust in that regard.
	 */
	@Test
	fun shouldBuildDummySequenceForDistortedEdgeView() {
		val sequence = createSequence(listOf(Point2D(0, 0)))
		assertEquals(0.0, sequence.size)
	}

	@Test
	fun shouldKeepCornerVisitsAndRemainderWhenStepSizeChanges() {
		val sequence = createSequence(listOf(Point2D(0, 0), Point2D(10, 0)), returnSequenceEndpoints = true)
		assertEquals(Point2D(0, 0), sequence.getCurrent())
		assertEquals(Point2D(0, 0), sequence.getNext(2.0))
		assertEquals(Point2D(2, 0), sequence.getCurrent())
		assertEquals(Point2D(2, 0), sequence.getNext(5.0))
		assertEquals(Point2D(7, 0), sequence.getNext(4.0))
		assertEquals(1.0, sequence.remainder)
		assertEquals(Point2D(10, 0), sequence.getNext(3.0))
		assertNull(sequence.getNext(3.0))
		assertNull(sequence.getCurrent())
		// An explicitly visited endpoint consumes a tick. Preserve its outgoing step as remainder.
		assertEquals(3.0, sequence.remainder)

		val successor = createSequence(listOf(Point2D(10, 0), Point2D(20, 0)), offset = sequence.remainder)
		assertEquals(Point2D(13, 0), successor.getNext(3.0))
	}

	@Test
	fun shouldVisitReverseCornersWithLargeStepsAndOffset() {
		val sequence = createSequence(
			listOf(Point2D(0, 0), Point2D(2, 0), Point2D(2, 2), Point2D(5, 2)),
			reverse = true,
			returnSequenceEndpoints = true,
			offset = 1.0,
		)
		assertEquals(Point2D(4, 2), sequence.getNext(8.0))
		assertEquals(Point2D(2, 2), sequence.getNext(8.0))
		assertEquals(Point2D(2, 0), sequence.getNext(8.0))
		assertEquals(Point2D(0, 0), sequence.getNext(8.0))
		assertNull(sequence.getNext(8.0))
	}

	@Test
	fun shouldCarrySpacingAcrossSeveralShortSegments() {
		val sequence = createSequence(
			listOf(Point2D(0, 0), Point2D(2, 0), Point2D(2, 2), Point2D(20, 2)),
			returnSequenceEndpoints = false,
			offset = 5.0,
		)
		assertForEachWithDistance(sequence, 8.0, listOf(Point2D(3, 2), Point2D(11, 2), Point2D(19, 2)))
	}

	@Test
	fun shouldTraverseDiagonalSegmentsInEitherDirection() {
		for (reverse in listOf(false, true)) {
			val sequence = createSequence(
				listOf(Point2D(0, 0), Point2D(6, 8), Point2D(6, 18)),
				reverse = reverse,
				returnSequenceEndpoints = true,
			)
			val expected = listOf(Point2D(0, 0), Point2D(3, 4), Point2D(6, 8), Point2D(6, 13), Point2D(6, 18))
			assertForEachWithDistance(sequence, 5.0, if (reverse) expected.reversed() else expected)
		}
	}

	@Test
	fun shouldHandleEmptyAndZeroLengthGeometry() {
		assertNull(createSequence(emptyList()).getNext(5.0))
		val sequence = createSequence(listOf(Point2D(3, 4), Point2D(3, 4)))
		assertEquals(Point2D(3, 4), sequence.getNext(5.0))
		assertNull(sequence.getNext(5.0))
	}

	@Test
	fun shouldReuseImmutableVerticesAtEndpoints() {
		val edge = GraphViewModule.getEdgeViewFactory().createEdgeView<Boolean>(mock())
		edge.addSegmentPoint(Point2D(0, 0))
		edge.addSegmentPoint(Point2D(10, 0))
		val sequence = EdgeViewPointSequence(edge, isReverse = false, returnSequenceEndPoint = true, offset = 0.0, size = 25.0)
		assertEquals(25.0, sequence.size)
		assertSame(edge.polyline.getPointAt(0), sequence.getNext(10.0))
		assertSame(edge.polyline.getPointAt(1), sequence.getNext(10.0))
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

	private fun createReverseSequence(points: List<Point2D>, offset: Double = 0.0): EdgeViewPointSequence =
		createSequence(points, true, offset = offset)

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
