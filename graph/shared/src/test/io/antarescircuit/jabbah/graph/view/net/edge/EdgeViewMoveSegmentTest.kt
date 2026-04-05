package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class EdgeViewMoveSegmentTest {

	private val builder: GraphViewBuilder<Boolean>

	init {
		GraphViewTestRule.configure()
		builder = GraphViewBuilder()
	}

	/** Test for fix of bug #48.*/
	@Test
	fun shouldAnnihilateUShapedSegments() {
		val v1 = builder.addVerticeView(createVerticeView(100, 200, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val edgeView = builder.connect(v1, v2)
		edgeView.moveSegment(0, Point2D(125, 200), Point2D(125, 300))

		edgeView.moveSegment(3, Point2D(150, 200), Point2D(120, 200))

		assertEquals(4, edgeView.segmentPointCount)
	}

	private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView {
		return TestVerticeView(loc = Point2D(x, y), inputDirection = dir, portViewLength = 20)
	}
}