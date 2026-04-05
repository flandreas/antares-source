package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EdgeViewImplTest {

	private val edgeViewFactory = GraphViewModule.getEdgeViewFactory()
	private val graphView: GraphView

	init {
		GraphViewTestRule.configure()
		graphView = GraphViewModule.graphViewFactory.create(null)
	}

	@Test
	fun shouldUpdateBoundingBox() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(100, 100))
		ev.addSegmentPoint(Point2D(200, 100))
		ev.addSegmentPoint(Point2D(200, 200))
		// Result includes line width and EdgeEndpointViews
		assertEquals(Rectangle2D(92, 92, 116, 116), ev.boundingBox as Rectangle2D)
	}

	@Test
	fun regularEdgeViewShouldNotBeDegenerated() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(10, 10))
		ev.addSegmentPoint(Point2D(20, 10))
		assertFalse(ev.isDegenerated)
	}

	@Test
	fun shouldDetermineEmptyDegeneration() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(10, 10))
		ev.addSegmentPoint(Point2D(10, 10))
		assertTrue(ev.isDegenerated)
	}

	@Test
	fun shouldDetermineOriginDegeneration() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(10, 10))
		ev.addSegmentPoint(Point2D(10, 10))
		ev.addSegmentPoint(Point2D(20, 10))
		assertTrue(ev.isDegenerated)
	}

	@Test
	fun shouldDetermineDestinationDegeneration() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(10, 10))
		ev.addSegmentPoint(Point2D(20, 10))
		ev.addSegmentPoint(Point2D(20, 10))
		assertTrue(ev.isDegenerated)
	}
	@Test
	fun testSegmentDegeneration() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		ev.addSegmentPoint(Point2D(100, 0))

		assertFalse(ev.isSegmentDegenerated(0))
		assertTrue(ev.isSegmentDegenerated(1))
	}

	@Test
	fun shouldCompactEqualPoints() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(10, 10))
		ev.addSegmentPoint(Point2D(10, 10))
		ev.addSegmentPoint(Point2D(20, 10))

		ev.compact()

		assertEquals(2, ev.segmentPointCount)
	}

	@Test
	fun shouldCompactHorizontally() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(10, 0))
		ev.addSegmentPoint(Point2D(20, 0))

		ev.compact()

		assertEquals(2, ev.segmentPointCount)
	}

	@Test
	fun shouldCompactVertically() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(0, 10))
		ev.addSegmentPoint(Point2D(0, 20))

		ev.compact()

		assertEquals(2, ev.segmentPointCount)
	}

	@Test
	fun shouldCalculateLengthWithoutPoints() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		assertEquals(0.0, ev.polyline.length)
	}

	@Test
	fun shouldCalculateLengthWithOnePoint() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		assertEquals(0.0, ev.polyline.length)
	}

	@Test
	fun shouldCalculateLength() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(0, 10))
		ev.addSegmentPoint(Point2D(20, 10))

		assertEquals(30.0, ev.polyline.length)
	}

	@Test
	fun shouldCalculateSegmentDirection() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		ev.addSegmentPoint(Point2D(100, 100))
		ev.addSegmentPoint(Point2D(0, 100))
		ev.addSegmentPoint(Point2D(0, 0))

		assertEquals(Direction.EAST, ev.getSegmentDirection(0)!!)
		assertEquals(Direction.SOUTH, ev.getSegmentDirection(1)!!)
		assertEquals(Direction.WEST, ev.getSegmentDirection(2)!!)
		assertEquals(Direction.NORTH, ev.getSegmentDirection(3)!!)
	}

	@Test
	fun shouldCalculateSegmentDirectionWithoutSnap() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(-364.0, -308.0))
		ev.addSegmentPoint(Point2D(-364.0, -294.0))
		ev.addSegmentPoint(Point2D(-364.5, -294.0))

		assertEquals(Direction.WEST, ev.getSegmentDirection(1))
	}

	@Test
	fun shouldFindSegmentWithMinimalArea() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(150, 100))
		ev.addSegmentPoint(Point2D(150, 0))
		ev.addSegmentPoint(Point2D(200, 0))

		assertEquals(0, ev.polyline.findSegment(150.0, 50.0, 1))
	}

	@Test
	fun shouldNotMoveIndividually() {
		val vv1 = TestVerticeView(loc = Point2D(0, 0), outputDirection = Direction.EAST)
		val vv2 = TestVerticeView(loc = Point2D(100, 0), inputDirection = Direction.WEST)
		graphView.add(vv1)
		graphView.add(vv2)
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		graphView.add(ev)
		ev.connectToOrigin(Connection(vv1, vv1.model.getOutput()))
		ev.connectToDestination(Connection(vv2, vv2.model.getOutput()))

		ev.prepareMoveBy(listOf(ev))
		ev.moveBy(0.0, 50.0)

		assertEquals(Point2D(0, 0), ev.getSegmentPoint(0))
		assertEquals(Point2D(100, 0), ev.getSegmentPoint(1))
	}

	@Test
	fun shouldMoveWithConnectableViews() {
		val vv1 = TestVerticeView(loc = Point2D(0, 0), outputDirection = Direction.EAST)
		val vv2 = TestVerticeView(loc = Point2D(100, 0), inputDirection = Direction.WEST)
		graphView.add(vv1)
		graphView.add(vv2)
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		graphView.add(ev)
		ev.connectToOrigin(Connection(vv1, vv1.model.getOutput()))
		ev.connectToDestination(Connection(vv2, vv2.model.getOutput()))

		ev.prepareMoveBy(listOf(vv1, vv2, ev))
		ev.moveBy(0.0, 50.0)

		assertEquals(Point2D(0, 50), ev.getSegmentPoint(0))
		assertEquals(Point2D(100, 50), ev.getSegmentPoint(1))
	}

	@Test
	fun shouldMoveUnaryConnected() {
		val vv1 = TestVerticeView(loc = Point2D(0, 0), outputDirection = Direction.EAST)
		graphView.add(vv1)
		val ev = edgeViewFactory.createEdgeView<Boolean>(graphView)
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		graphView.add(ev)
		ev.connectToOrigin(Connection(vv1, vv1.model.getOutput()))

		ev.prepareMoveBy(listOf(vv1, ev))
		ev.moveBy(0.0, 50.0)

		assertEquals(Point2D(0, 50), ev.getSegmentPoint(0))
		assertEquals(Point2D(100, 50), ev.getSegmentPoint(1))
	}
}