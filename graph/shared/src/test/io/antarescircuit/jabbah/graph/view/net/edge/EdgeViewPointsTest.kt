package io.antarescircuit.jabbah.graph.view.net.edge

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.polyline.PolylineShapeImpl
import io.antarescircuit.jabbah.graph.view.EdgeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EdgeViewPointsTest {

	@Test
	fun shouldCarrySpacingAcrossBends() {
		val edge = createEdge(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10), Point2D(0, 10))

		assertEquals(
			listOf(Point2D(5, 0), Point2D(10, 3), Point2D(9, 10), Point2D(1, 10)),
			collectPoints(edge, distance = 8.0, offset = 5.0),
		)
	}

	@Test
	fun shouldCarryOffsetAcrossSeveralShortSegments() {
		val edge = createEdge(Point2D(0, 0), Point2D(2, 0), Point2D(2, 2), Point2D(20, 2))

		assertEquals(
			listOf(Point2D(3, 2), Point2D(11, 2), Point2D(19, 2)),
			collectPoints(edge, distance = 8.0, offset = 5.0),
		)
	}

	@Test
	fun shouldVisitExactCornersAndDestinationOnlyOnce() {
		val edge = createEdge(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10))

		assertEquals(
			listOf(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10)),
			collectPoints(edge, distance = 10.0),
		)
		assertEquals(
			listOf(Point2D(10, 0), Point2D(10, 10)),
			collectPoints(edge, distance = 10.0, offset = 10.0),
		)
	}

	@Test
	fun shouldNotForceDestinationOntoSpacingGrid() {
		val edge = createEdge(Point2D(0, 0), Point2D(20, 0))

		assertEquals(listOf(Point2D(0, 0), Point2D(15, 0)), collectPoints(edge, distance = 15.0))
		assertEquals(emptyList(), collectPoints(edge, distance = 15.0, offset = 21.0))
	}

	@Test
	fun shouldTraverseDiagonalAndDescendingSegments() {
		val edge = createEdge(Point2D(10, 10), Point2D(4, 2), Point2D(-6, 2))
		val points = collectPoints(edge, distance = 5.0)

		assertEquals(5, points.size)
		val expected = listOf(Point2D(10, 10), Point2D(7, 6), Point2D(4, 2), Point2D(-1, 2), Point2D(-6, 2))
		points.forEachIndexed { index, point ->
			assertEquals(expected[index].x, point.x, 1E-10)
			assertEquals(expected[index].y, point.y, 1E-10)
		}
	}

	@Test
	fun shouldSkipRepeatedVerticesWithoutLosingSpacing() {
		val edge = createEdge(Point2D(0, 0), Point2D(0, 0), Point2D(10, 0), Point2D(10, 0), Point2D(10, 10))

		assertEquals(
			listOf(Point2D(0, 0), Point2D(10, 0), Point2D(10, 10)),
			collectPoints(edge, distance = 10.0),
		)
		assertEquals(
			listOf(Point2D(5, 0), Point2D(10, 5)),
			collectPoints(edge, distance = 10.0, offset = 5.0),
		)
	}

	@Test
	fun shouldHandleEmptyAndDegenerateEdges() {
		assertEquals(emptyList(), collectPoints(createEdge(), distance = 5.0))
		for (edge in listOf(createEdge(Point2D(3, 4)), createEdge(Point2D(3, 4), Point2D(3, 4)))) {
			assertEquals(listOf(Point2D(3, 4)), collectPoints(edge, distance = 5.0))
			assertEquals(emptyList(), collectPoints(edge, distance = 5.0, offset = 1.0))
		}
	}

	@Test
	fun shouldReadUpdatedGeometryOnEveryTraversal() {
		val edge = createEdge(Point2D(0, 0), Point2D(10, 0))
		assertEquals(listOf(Point2D(0, 0), Point2D(10, 0)), collectPoints(edge, distance = 10.0))

		edge.polyline.setPointAt(1, 0.0, 20.0)

		assertEquals(
			listOf(Point2D(0, 0), Point2D(0, 10), Point2D(0, 20)),
			collectPoints(edge, distance = 10.0),
		)
	}

	@Test
	fun shouldRejectInvalidSpacingAndOffsets() {
		val edge = createEdge(Point2D(0, 0), Point2D(10, 0))
		for (distance in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
			assertFailsWith<IllegalArgumentException> { collectPoints(edge, distance) }
		}
		for (offset in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
			assertFailsWith<IllegalArgumentException> { collectPoints(edge, distance = 5.0, offset = offset) }
		}
	}

	private fun createEdge(vararg points: Point2D): EdgeView<Boolean> {
		val shape = PolylineShapeImpl(*points)
		return mock<EdgeView<Boolean>>().also { edge ->
			every { edge.polyline } returns shape
		}
	}

	private fun collectPoints(edge: EdgeView<*>, distance: Double, offset: Double = 0.0): List<Point2D> {
		val points = mutableListOf<Point2D>()
		edge.forEachPoint(distance, offset) { x, y -> points.add(Point2D(x, y)) }
		return points
	}
}
