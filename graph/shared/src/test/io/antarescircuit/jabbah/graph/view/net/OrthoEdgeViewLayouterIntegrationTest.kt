package io.antarescircuit.jabbah.graph.view.net

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Direction.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.net.edge.LayoutType
import io.antarescircuit.jabbah.graph.view.net.edge.OrthoEdgeViewLayouter
import io.antarescircuit.jabbah.graph.view.net.node.NodeViewImpl
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

/**
 * Integration tests for integrating [OrthoEdgeViewLayouter] and [NodeViewImpl].
 */
class OrthoEdgeViewLayouterIntegrationTest {

	private val builder: GraphViewBuilder<Boolean>

	init {
		GraphViewTestRule.configure()
		builder= GraphViewBuilder()
	}

	@Test
	fun shouldSplitHorizontalEdgeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 200, WEST))
		val origEdgeView = builder.connect(v1, v2)

		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		assertEquals(2, splitResult.tailEdgeView.segmentPointCount)
		assertFalse(splitResult.tailEdgeView.layout.isAdjusted)

		assertEquals(3, splitResult.newEdgeView.segmentPointCount)
		assertFalse(splitResult.newEdgeView.layout.isAdjusted)

		assertEquals(2, origEdgeView.segmentPointCount)
		assertFalse(origEdgeView.layout.isAdjusted)
	}

	@Test
	fun shouldSplitEdgeViewAtCorner() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 200, WEST))
		val origEdgeView = builder.connect(v1, v3)

		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v2)

		assertEquals(3, splitResult.tailEdgeView.segmentPointCount)
		assertTrue(splitResult.tailEdgeView.layout.isAdjusted)

		assertEquals(2, splitResult.newEdgeView.segmentPointCount)
		assertFalse(splitResult.newEdgeView.layout.isAdjusted)

		assertEquals(2, origEdgeView.segmentPointCount)
		assertFalse(origEdgeView.layout.isAdjusted)
	}

	@Test
	fun shouldLayoutWestOfNodeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val v3 = builder.addVerticeView(createVerticeView(150, 0, SOUTH))
		val origEdgeView = builder.connect(v1, v2)
		builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		v1.moveBy(-10.0, 0.0)

		assertEquals(2, origEdgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutEastOfNodeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val v4 = builder.addVerticeView(createVerticeView(150, 0, SOUTH))
		val origEdgeView = builder.connect(v1, v2)
		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v4)

		v2.moveBy(0.0, -10.0)

		assertEquals(4, splitResult.tailEdgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutNorthOfNodeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 0, WEST))
		val origEdgeView = builder.connect(v1, v2)
		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		assertEquals(3, splitResult.newEdgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutVerticalOpenVerticeView() {
		val v = builder.addVerticeView(createVerticeView(100, 100, SOUTH))
		val edgeView = builder.connectInputOpen(v, Point2D(100, 200))
		assertEquals(2, edgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutVerticalSplitOpenVerticeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val ev = builder.connect(v1, v2)

		val splitResult = builder.split(ev, 0, Point2D(150, 100), null as PortView<Boolean>?)
		splitResult.newEdgeView.moveDestinationEndPoint(200.0, 200.0)

		assertEquals(3, splitResult.newEdgeView.segmentPointCount)
		assertEquals(SOUTH, splitResult.newEdgeView.getSegmentDirection(0))
	}

	@Test
	fun shouldCooperateWithNonLayoutEdgeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 100, WEST))
		val origEdgeView = builder.connect(v1, v2)
		origEdgeView.layout.type = LayoutType.NONE
		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		assertEquals(SOUTH, splitResult.newEdgeView.getSegmentDirection(0))
	}

	@Test
	fun shouldNotDistortZShapedEdgeViewWhenSplitting() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, WEST))
		val v2 = builder.addVerticeView(createVerticeView(200, 200, WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 300, WEST))
		val origEdgeView = builder.connect(v1, v2)

		builder.split(origEdgeView, 2, Point2D(170, 200), v3)

		assertEquals(4, origEdgeView.segmentPointCount)
		assertEquals(Point2D(100, 100), origEdgeView.getSegmentPoint(0))
		assertEquals(Point2D(150, 100), origEdgeView.getSegmentPoint(1))
		assertEquals(Point2D(150, 200), origEdgeView.getSegmentPoint(2))
		assertEquals(Point2D(170, 200), origEdgeView.getSegmentPoint(3))
	}

	/** Regression test for GitHub issue #213.*/
	@Test
	fun shouldNotDistortLayoutWhenSplittingUpwards() {
		val v1 = builder.addVerticeView(createVerticeView(100, 300, EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 200, SOUTH))
		val v3 = builder.addVerticeView(createVerticeView(200, 100, SOUTH))
		val v4 = builder.addVerticeView(createVerticeView(200, 0, SOUTH))
		val ev12 = builder.connect(v1, v2)
		val ev13 = builder.split(ev12, 0, Point2D(150, 300), v3)

		assertEquals(4, ev13.newEdgeView.segmentPointCount)
		assertEquals(Point2D(150, 300), ev13.newEdgeView.polyline.getPointAt(0))
		assertEquals(Point2D(150, 114), ev13.newEdgeView.polyline.getPointAt(1))
		assertEquals(Point2D(200, 114), ev13.newEdgeView.polyline.getPointAt(2))
		assertEquals(Point2D(200, 100), ev13.newEdgeView.polyline.getPointAt(3))

		val ev14 = builder.split(ev13.newEdgeView, 0, Point2D(150, 200), v4)

		// Layout of previous EdgeView
		assertEquals(2, ev13.newEdgeView.polyline.pointsCount)
		assertEquals(Point2D(150, 300), ev13.newEdgeView.polyline.getPointAt(0))
		assertEquals(Point2D(150, 200), ev13.newEdgeView.polyline.getPointAt(1))

		// Layout of new EdgeView
		assertEquals(3, ev14.newEdgeView.segmentPointCount)
		assertEquals(Point2D(150, 200), ev14.newEdgeView.polyline.getPointAt(0))
		assertEquals(Point2D(200, 200), ev14.newEdgeView.polyline.getPointAt(1))
		assertEquals(Point2D(200, 0), ev14.newEdgeView.polyline.getPointAt(2))
	}

	@Test
	fun shouldNotOverlapEdgeViewsVertically() {
		val v1 = builder.addVerticeView(createVerticeView(0, 300, EAST))
		val v2 = builder.addVerticeView(createVerticeView(100, 0, WEST))
		val v3 = builder.addVerticeView(createVerticeView(0, 400, EAST))
		val v4 = builder.addVerticeView(createVerticeView(100, 100, WEST))
		builder.connect(v1, v2)
		val ev34 = builder.connect(v3, v4)

		// Require layout right to the middle in order to avoid intersections
		assertEquals(57.0, ev34.polyline.getPointAt(1).x)
	}

	@Test
	fun shouldNotOverlapEdgeViewsHorizontally() {
		val v1 = builder.addVerticeView(createVerticeView(0, 0, NORTH, SOUTH))
		val v2 = builder.addVerticeView(createVerticeView(300, 200, NORTH))
		val v3 = builder.addVerticeView(createVerticeView(100, 0, NORTH, SOUTH))
		val v4 = builder.addVerticeView(createVerticeView(400, 200, NORTH))

		builder.connect(v1, v2)
		val ev34 = builder.connect(v3, v4)

		assertEquals(93.0, ev34.polyline.getPointAt(1).y)
	}

	private fun createVerticeView(x: Int, y: Int, dir: Direction, outDir: Direction = EAST): TestVerticeView =
		TestVerticeView(loc = Point2D(x, y), inputDirection = dir, outputDirection =  outDir, portViewLength = 20)
}