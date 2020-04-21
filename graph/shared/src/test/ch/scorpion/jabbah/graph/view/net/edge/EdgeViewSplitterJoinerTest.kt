package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

/** Unit tests for [EdgeViewSplitterJoiner]. */
class EdgeViewSplitterJoinerTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val service = GraphViewModule.graphViewConnectService
	private val edgeViewFactory = GraphViewModule.getEdgeViewFactory<Boolean>()
	private val gv = GraphViewModule.createGraphView()

	@Test
	fun shouldSplitInMiddleOfSegment() {
		val ev = edgeViewFactory.createEdgeView()
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		ev.addSegmentPoint(Point2D(100, 50))
		ev.addSegmentPoint(Point2D(200, 50))

		val newEV = EdgeViewSplitterJoiner.split(ev, 1, Point2D(100, 25)) { edgeViewFactory.createEdgeView(it) }

		kotlin.test.assertEquals(3, ev.segmentPointCount)
		kotlin.test.assertEquals(Point2D(0, 0), ev.getSegmentPoint(0))
		kotlin.test.assertEquals(Point2D(100, 0), ev.getSegmentPoint(1))
		kotlin.test.assertEquals(Point2D(100, 25), ev.getSegmentPoint(2))

		kotlin.test.assertEquals(3, newEV.segmentPointCount)
		kotlin.test.assertEquals(Point2D(100, 25), newEV.getSegmentPoint(0))
		kotlin.test.assertEquals(Point2D(100, 50), newEV.getSegmentPoint(1))
		kotlin.test.assertEquals(Point2D(200, 50), newEV.getSegmentPoint(2))
	}

	@Test
	fun shouldSplitAtStartOfSegment() {
		val ev = edgeViewFactory.createEdgeView()
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		ev.addSegmentPoint(Point2D(100, 50))
		ev.addSegmentPoint(Point2D(200, 50))

		val newEV = EdgeViewSplitterJoiner.split(ev, 1, Point2D(100, 0)) { edgeViewFactory.createEdgeView(it) }

		kotlin.test.assertEquals(2, ev.segmentPointCount)
		kotlin.test.assertEquals(Point2D(0, 0), ev.getSegmentPoint(0))
		kotlin.test.assertEquals(Point2D(100, 0), ev.getSegmentPoint(1))

		kotlin.test.assertEquals(3, newEV.segmentPointCount)
		kotlin.test.assertEquals(Point2D(100, 0), newEV.getSegmentPoint(0))
		kotlin.test.assertEquals(Point2D(100, 50), newEV.getSegmentPoint(1))
		kotlin.test.assertEquals(Point2D(200, 50), newEV.getSegmentPoint(2))
	}

	@Test
	fun shouldUnconnectOutInWhenSplitting() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(0, 0))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(100, 0))
		gv.add(vv1).add(vv2)
		val ev = service.addConnection<Boolean>(gv, vv1, vv2)

		val tail = EdgeViewSplitterJoiner.split(ev, 0, Point2D(50, 0)) { edgeViewFactory.createEdgeView(it) }

		assertSame(vv1.model.getOutput(), ev.origin!!.port)
		assertNull(ev.destination?.port)
		assertSame(vv2.model.getInput(), tail.destination?.port)
		assertNull(tail.origin?.port)
	}

	@Test
	fun shouldUnconnectInOutInWhenSplitting() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(0, 0), vertice = TestVertice(inOut = true))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(100, 0))
		gv.add(vv1).add(vv2)
		val ev = service.addConnection<Boolean>(gv, vv1, vv2)

		val tail = EdgeViewSplitterJoiner.split(ev, 0, Point2D(50, 0)) { edgeViewFactory.createEdgeView(it) }

		assertSame(vv1.model.getOutput(), ev.origin?.port)
		assertNull(ev.destination?.port)
		assertSame(vv2.model.getInput(), tail.destination?.port)
		assertNull(tail.origin?.port)
	}

	@Test
	fun shouldJoinOtherHeadWithTail() {
		val ev1 = edgeViewFactory.createEdgeView()
		ev1.addSegmentPoint(Point2D(0, 0))
		ev1.addSegmentPoint(Point2D(100, 0))
		gv.add(ev1)

		val ev2 = edgeViewFactory.createEdgeView(ev1.model)
		ev2.addSegmentPoint(Point2D(100, 0))
		ev2.addSegmentPoint(Point2D(200, 0))
		gv.add(ev2)

		val vv = TestVerticeView()
		ev2.connectToDestination(Connection(vv, vv.model.getInput()))

		EdgeViewSplitterJoiner.join(ev1, ev2)

		kotlin.test.assertEquals(2, ev1.segmentPointCount)
		kotlin.test.assertEquals(Point2D(0, 0), ev1.getSegmentPoint(0))
		kotlin.test.assertEquals(Point2D(200, 0), ev1.getSegmentPoint(1))

		kotlin.test.assertEquals(ev1.destination!!.connectableView as TestVerticeView, vv)
		kotlin.test.assertNull(ev2.destination)
	}

	@Test
	fun shouldJoinOtherTailWithHead() {
		val ev1 = edgeViewFactory.createEdgeView()
		ev1.addSegmentPoint(Point2D(0, 0))
		ev1.addSegmentPoint(Point2D(100, 0))
		gv.add(ev1)

		val ev2 = edgeViewFactory.createEdgeView(ev1.model)
		ev2.addSegmentPoint(Point2D(100, 0))
		ev2.addSegmentPoint(Point2D(200, 0))
		gv.add(ev2)

		val vv = TestVerticeView()
		ev1.connectToOrigin(Connection(vv, vv.model.getOutput()))

		EdgeViewSplitterJoiner.join(ev2, ev1)

		kotlin.test.assertEquals(2, ev2.segmentPointCount)
		kotlin.test.assertEquals(Point2D(0, 0), ev2.getSegmentPoint(0))
		kotlin.test.assertEquals(Point2D(200, 0), ev2.getSegmentPoint(1))

		kotlin.test.assertEquals(vv, ev2.origin!!.connectableView as TestVerticeView)
		kotlin.test.assertNull(ev1.origin)
	}

	@Test
	fun shouldJoinOtherHeadWithHead() {
		val ev1 = edgeViewFactory.createEdgeView()
		ev1.addSegmentPoint(Point2D(0, 0))
		ev1.addSegmentPoint(Point2D(100, 0))
		gv.add(ev1)

		val ev2 = edgeViewFactory.createEdgeView(ev1.model)
		ev2.addSegmentPoint(Point2D(0, 0))
		ev2.addSegmentPoint(Point2D(-100, 0))
		gv.add(ev2)

		val vv1 = TestVerticeView()
		ev1.connectToDestination(Connection(vv1, vv1.model.getOutput()))

		val vv2 = TestVerticeView()
		ev2.connectToDestination(Connection(vv2, vv2.model.getOutput()))

		EdgeViewSplitterJoiner.join(ev1, ev2)

		kotlin.test.assertEquals(2, ev1.segmentPointCount)
		kotlin.test.assertEquals(Point2D(-100, 0), ev1.getSegmentPoint(0))
		kotlin.test.assertEquals(Point2D(100, 0), ev1.getSegmentPoint(1))

		kotlin.test.assertEquals(vv2, ev1.origin!!.connectableView as TestVerticeView)
		kotlin.test.assertEquals(vv1, ev1.destination!!.connectableView as TestVerticeView)
	}
}