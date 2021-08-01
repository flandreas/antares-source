package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType
import ch.scorpion.jabbah.graph.view.net.edge.OrthoEdgeViewLayouter
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for integrating [OrthoEdgeViewLayouter] and [NodeViewImpl].
 */
class OrthoEdgeViewLayouterIntegrationTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()


	@Test
	fun shouldSplitHorizontalEdgeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 200, Direction.WEST))
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
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 200, Direction.WEST))
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
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(150, 0, Direction.SOUTH))
		val origEdgeView = builder.connect(v1, v2)
		builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		v1.moveBy(-10.0, 0.0)

		assertEquals(2, origEdgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutEastOfNodeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val v4 = builder.addVerticeView(createVerticeView(150, 0, Direction.SOUTH))
		val origEdgeView = builder.connect(v1, v2)
		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v4)

		v2.moveBy(0.0, -10.0)

		assertEquals(4, splitResult.tailEdgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutNorthOfNodeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 0, Direction.WEST))
		val origEdgeView = builder.connect(v1, v2)
		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		assertEquals(3, splitResult.newEdgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutVerticalOpenVerticeView() {
		val v = builder.addVerticeView(createVerticeView(100, 100, Direction.SOUTH))
		val edgeView = builder.connectInputOpen(v, Point2D(100, 200))
		assertEquals(2, edgeView.segmentPointCount)
	}

	@Test
	fun shouldLayoutVerticalSplitOpenVerticeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val ev = builder.connect(v1, v2)

		val splitResult = builder.split(ev, 0, Point2D(150, 100), null)
		splitResult.newEdgeView.moveDestinationEndPoint(200.0, 200.0)

		assertEquals(3, splitResult.newEdgeView.segmentPointCount)
		assertEquals(Direction.SOUTH, splitResult.newEdgeView.getSegmentDirection(0))
	}

	@Test
	fun shouldCooperateWithNonLayoutEdgeView() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
		val origEdgeView = builder.connect(v1, v2)
		origEdgeView.layout.type = LayoutType.NONE
		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		assertEquals(Direction.SOUTH, splitResult.newEdgeView.getSegmentDirection(0))
	}

	@Test
	fun shouldNotDistortZShapedEdgeViewWhenSplitting() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.WEST))
		val v2 = builder.addVerticeView(createVerticeView(200, 200, Direction.WEST))
		val v3 = builder.addVerticeView(createVerticeView(200, 300, Direction.WEST))
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
		val v1 = builder.addVerticeView(createVerticeView(100, 300, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 200, Direction.SOUTH))
		val v3 = builder.addVerticeView(createVerticeView(200, 100, Direction.SOUTH))
		val v4 = builder.addVerticeView(createVerticeView(200, 0, Direction.SOUTH))
		val ev12 = builder.connect(v1, v2)
		val ev13 = builder.split(ev12, 0, Point2D(150, 300), v3)

		assertEquals(4, ev13.newEdgeView.segmentPointCount)
		assertEquals(Point2D(150, 300), ev13.newEdgeView.polyline.getPointAt(0))
		assertEquals(Point2D(150, 200), ev13.newEdgeView.polyline.getPointAt(1))
		assertEquals(Point2D(200, 200), ev13.newEdgeView.polyline.getPointAt(2))
		assertEquals(Point2D(200, 100), ev13.newEdgeView.polyline.getPointAt(3))

		val ev14 = builder.split(ev13.newEdgeView, 0, Point2D(150, 200), v4)

		// Layout of previous EdgeView
		assertEquals(2, ev13.newEdgeView.polyline.pointsCount)
		assertEquals(Point2D(150, 300), ev13.newEdgeView.polyline.getPointAt(0))
		assertEquals(Point2D(150, 200), ev13.newEdgeView.polyline.getPointAt(1))

		// Layout of new EdgeView
		assertEquals(4, ev14.newEdgeView.segmentPointCount)
		assertEquals(Point2D(150, 200), ev14.newEdgeView.polyline.getPointAt(0))
		assertEquals(Point2D(150, 100), ev14.newEdgeView.polyline.getPointAt(1))
		assertEquals(Point2D(200, 100), ev14.newEdgeView.polyline.getPointAt(2))
		assertEquals(Point2D(200, 0), ev14.newEdgeView.polyline.getPointAt(3))
	}

	private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView =
		TestVerticeView(loc = Point2D(x, y), inputDirection = dir, portViewLength = 20)
}