package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [OrthoEdgeViewLayout].
 */
class OrthoEdgeViewLayoutTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private lateinit var graphView: GraphView<*>

	@BeforeTest
	fun setup() {
		graphView = mockk()
		every { graphView.snapper } returns null
	}

	@Test
	fun layoutEastEast() {
		val points = OrthoEdgeViewLayout.layout(
			null,
			graphView,
			LayoutBoundary(
				point = Point2D(0, 0),
				directions = setOf(Direction.EAST),
				isPort = true),
			LayoutBoundary(
				point = Point2D(100, 100),
				directions = setOf(Direction.EAST),
				isPort = true))

		assertEquals(4, points.size)
		assertEquals(Point2D(0, 0), points[0])
		assertEquals(Point2D(50, 0), points[1])
		assertEquals(Point2D(50, 100), points[2])
		assertEquals(Point2D(100, 100), points[3])
	}

	@Test
	fun layoutEastEastDegenerated() {
		val points = OrthoEdgeViewLayout.layout(
			null,
			graphView,
			LayoutBoundary(
				point = Point2D(100, 0),
				directions = setOf(Direction.EAST),
				isPort = true),
			LayoutBoundary(
				point = Point2D(0, 0),
				directions = setOf(Direction.EAST),
				isPort = true))

		assertEquals(2, points.size)
		assertEquals(Point2D(100, 0), points[0])
		assertEquals(Point2D(0, 0), points[1])
	}

	@Test
	fun shouldNotFailWithEmptyPolyline() {
		val points = OrthoEdgeViewLayout.layout(
			null,
			graphView,
			LayoutBoundary(
				point = Point2D(0, 0),
				directions = setOf(Direction.EAST),
				isPort = true),
			LayoutBoundary(
				point = Point2D(0, 0),
				directions = setOf(Direction.EAST),
				isPort = true))

		assertEquals(2, points.size)
	}

	@Test
	fun layoutSouthOpen() {
		val points = OrthoEdgeViewLayout.layout(
			null,
			graphView,
			LayoutBoundary(
				point = Point2D(0, 0),
				directions = setOf(Direction.EAST),
				isPort = true),
			LayoutBoundary(
				point = Point2D(100, 100),
				directions = setOf(Direction.WEST),
				isPort = false))

		assertEquals(3, points.size)
	}
}
