package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NoneEdgeViewLayouterTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val edgeViewFactory = GraphViewModule.getEdgeViewFactory()
	private lateinit var graphView: GraphView

	@BeforeTest
	fun setup() {
		graphView = mockk()
		every { graphView.snapper } returns null
	}

	@Test
	fun shouldLayoutDestination() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
			.addSegmentPoint(Point2D(0, 0))
			.addSegmentPoint(Point2D(50, 0))
			.addSegmentPoint(Point2D(50, 100))
			.addSegmentPoint(Point2D(100, 100))

		NoneEdgeViewLayouter.layoutDestination(
			ev,
			graphView,
			LayoutBoundary(
				point = Point2D(50, 100),
				directions = setOf(Direction.EAST),
				isPort = true),
			LayoutBoundary(
				point = Point2D(100, 120),
				directions = setOf(Direction.EAST),
				isPort = true),
			origPointIndex = 0,
			compact = false)

		assertEquals(4, ev.polyline.pointsCount)
		assertEquals(Point2D(0, 0), ev.polyline.getPointAt(0))
		assertEquals(Point2D(50, 0), ev.polyline.getPointAt(1))
		assertEquals(Point2D(50, 100), ev.polyline.getPointAt(2))
		assertEquals(Point2D(100, 120), ev.polyline.getPointAt(3))
	}
}