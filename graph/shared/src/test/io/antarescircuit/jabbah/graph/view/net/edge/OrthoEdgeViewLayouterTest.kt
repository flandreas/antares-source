package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.LogLevel
import io.antarescircuit.jabbah.base.LogSystem
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.model.polyline.CompactablePolyline
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrthoEdgeViewLayouterTest {

	private lateinit var graphView: GraphView

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		graphView = mock()
		every { graphView.snapper } returns null
		every { graphView.getEdgeViews()} returns listOf<EdgeView<*>>().toImmutableList()
	}

	@Test
	fun layoutEastEast() {
		val points = OrthoEdgeViewLayouter.layout(
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
		val points = OrthoEdgeViewLayouter.layout(
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
		val points = OrthoEdgeViewLayouter.layout(
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
		val points = OrthoEdgeViewLayouter.layout(
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

		assertEquals(4, points.size)
	}

	@Test
	fun shouldNotRequireBeginDirection() {
		// This is used for interactive adjusting, where two points at the begin Port
		// lay at the same position, and the second one is adjusted to snap the end Port,
		// resulting in a degenerated begin segment, and hence a missing begin Direction.
		val points = OrthoEdgeViewLayouter.layout(
			null,
			graphView,
			LayoutBoundary(
				point = Point2D(0, 0),
				directions = setOf(),
				isPort = true),
			LayoutBoundary(
				point = Point2D(100, 100),
				directions = setOf(Direction.WEST),
				isPort = true))

		assertEquals(4, points.size)
	}

	// #1097
	@Test
	fun shouldRespectBeginDirection() {
		LogSystem.level = LogLevel.Trace
		val points = OrthoEdgeViewLayouter.layout(
			null,
			graphView,
			LayoutBoundary(
				point = Point2D(170, 100),
				directions = setOf(Direction.SOUTH),
				isPort = false),
			LayoutBoundary(
				point = Point2D(150, 200),
				directions = emptySet(),
				isPort = false))

		assertTrue(CompactablePolyline(points).isOrthogonal)
		assertEquals(3, points.size)
		assertEquals(Point2D(170, 100), points[0])
		assertEquals(Point2D(170, 200), points[1])
		assertEquals(Point2D(150, 200), points[2])
	}
}
