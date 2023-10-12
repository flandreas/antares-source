package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
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
	private val edgeViewFactory = GraphViewModule.getEdgeViewFactory()
	private val gv = GraphViewModule.graphViewFactory.create(null)

	@Test
	fun shouldSplitInMiddleOfSegment() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(gv)
		gv.add(ev)

		splitInMiddleOfSegmentImpl(ev)
	}

	@Test
	fun shouldSplitWithBlockStyle() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(gv)
		gv.add(ev)
		ev.netView?.style = NetViewStyle.BLOCK

		splitInMiddleOfSegmentImpl(ev)
	}

	private fun splitInMiddleOfSegmentImpl(ev: EdgeView<Boolean>): EdgeView<Boolean> {
		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		ev.addSegmentPoint(Point2D(100, 50))
		ev.addSegmentPoint(Point2D(200, 50))

		val newEV = EdgeViewSplitterJoiner.split(ev, 1, Point2D(100, 25)) { edgeViewFactory.createEdgeView(gv, it) }

		assertEquals(3, ev.segmentPointCount)
		assertEquals(Point2D(0, 0), ev.getSegmentPoint(0))
		assertEquals(Point2D(100, 0), ev.getSegmentPoint(1))
		assertEquals(Point2D(100, 25), ev.getSegmentPoint(2))

		assertEquals(3, newEV.segmentPointCount)
		assertEquals(Point2D(100, 25), newEV.getSegmentPoint(0))
		assertEquals(Point2D(100, 50), newEV.getSegmentPoint(1))
		assertEquals(Point2D(200, 50), newEV.getSegmentPoint(2))

		return newEV
	}

	@Test
	fun shouldSplitAtStartOfSegment() {
		val ev = edgeViewFactory.createEdgeView<Boolean>(gv)
		gv.add(ev)

		ev.addSegmentPoint(Point2D(0, 0))
		ev.addSegmentPoint(Point2D(100, 0))
		ev.addSegmentPoint(Point2D(100, 50))
		ev.addSegmentPoint(Point2D(200, 50))

		val newEV = EdgeViewSplitterJoiner.split(ev, 1, Point2D(100, 0)) { edgeViewFactory.createEdgeView(gv, it) }

		assertEquals(2, ev.segmentPointCount)
		assertEquals(Point2D(0, 0), ev.getSegmentPoint(0))
		assertEquals(Point2D(100, 0), ev.getSegmentPoint(1))

		assertEquals(3, newEV.segmentPointCount)
		assertEquals(Point2D(100, 0), newEV.getSegmentPoint(0))
		assertEquals(Point2D(100, 50), newEV.getSegmentPoint(1))
		assertEquals(Point2D(200, 50), newEV.getSegmentPoint(2))
	}

	@Test
	fun shouldUnconnectOutInWhenSplitting() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(0, 0))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(100, 0))
		gv.add(vv1).add(vv2)
		val ev = service.addConnection<Boolean>(gv, vv1, vv2)

		val tail = EdgeViewSplitterJoiner.split(ev, 0, Point2D(50, 0)) { edgeViewFactory.createEdgeView(gv, it) }

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

		val tail = EdgeViewSplitterJoiner.split(ev, 0, Point2D(50, 0)) { edgeViewFactory.createEdgeView(gv, it) }

		assertSame(vv1.model.getOutput(), ev.origin?.port)
		assertNull(ev.destination?.port)
		assertSame(vv2.model.getInput(), tail.destination?.port)
		assertNull(tail.origin?.port)
	}

	@Test
	fun shouldJoinOtherHeadWithTail() {
		val ev1 = edgeViewFactory.createEdgeView<Boolean>(gv)
		ev1.addSegmentPoint(Point2D(0, 0))
		ev1.addSegmentPoint(Point2D(100, 0))
		gv.add(ev1)

		val ev2 = edgeViewFactory.createEdgeView(gv, ev1.netView!!)
		ev2.addSegmentPoint(Point2D(100, 0))
		ev2.addSegmentPoint(Point2D(200, 0))
		gv.add(ev2)

		val vv = TestVerticeView()
		ev2.connectToDestination(Connection(vv, vv.model.getInput()))

		EdgeViewSplitterJoiner.join(ev1, ev2)

		assertEquals(2, ev1.segmentPointCount)
		assertEquals(Point2D(0, 0), ev1.getSegmentPoint(0))
		assertEquals(Point2D(200, 0), ev1.getSegmentPoint(1))

		assertEquals(ev1.destination!!.connectableView as TestVerticeView, vv)
		assertNull(ev2.destination)
	}

	@Test
	fun shouldJoinOtherTailWithHead() {
		val ev1 = edgeViewFactory.createEdgeView<Boolean>(gv)
		ev1.addSegmentPoint(Point2D(0, 0))
		ev1.addSegmentPoint(Point2D(100, 0))
		gv.add(ev1)

		val ev2 = edgeViewFactory.createEdgeView(gv, ev1.netView!!)
		ev2.addSegmentPoint(Point2D(100, 0))
		ev2.addSegmentPoint(Point2D(200, 0))
		gv.add(ev2)

		val vv = TestVerticeView()
		ev1.connectToOrigin(Connection(vv, vv.model.getOutput()))

		EdgeViewSplitterJoiner.join(ev2, ev1)

		assertEquals(2, ev2.segmentPointCount)
		assertEquals(Point2D(0, 0), ev2.getSegmentPoint(0))
		assertEquals(Point2D(200, 0), ev2.getSegmentPoint(1))

		assertEquals(vv, ev2.origin!!.connectableView as TestVerticeView)
		assertNull(ev1.origin)
	}

	@Test
	fun shouldJoinOtherHeadWithHead() {
		val ev1 = edgeViewFactory.createEdgeView<Boolean>(gv)
		ev1.addSegmentPoint(Point2D(0, 0))
		ev1.addSegmentPoint(Point2D(100, 0))
		gv.add(ev1)

		val ev2 = edgeViewFactory.createEdgeView(gv, ev1.netView!!)
		ev2.addSegmentPoint(Point2D(0, 0))
		ev2.addSegmentPoint(Point2D(-100, 0))
		gv.add(ev2)

		val vv1 = TestVerticeView()
		ev1.connectToDestination(Connection(vv1, vv1.model.getOutput()))

		val vv2 = TestVerticeView()
		ev2.connectToDestination(Connection(vv2, vv2.model.getOutput()))

		EdgeViewSplitterJoiner.join(ev1, ev2)

		assertEquals(2, ev1.segmentPointCount)
		assertEquals(Point2D(-100, 0), ev1.getSegmentPoint(0))
		assertEquals(Point2D(100, 0), ev1.getSegmentPoint(1))

		assertEquals(vv2, ev1.origin!!.connectableView as TestVerticeView)
		assertEquals(vv1, ev1.destination!!.connectableView as TestVerticeView)
	}

	@Test
	fun shouldJoinOtherTailWithTail() {
		val ev1 = edgeViewFactory.createEdgeView<Boolean>(gv)
		ev1.addSegmentPoint(Point2D(0, 0))
		ev1.addSegmentPoint(Point2D(100, 0))
		gv.add(ev1)

		val ev2 = edgeViewFactory.createEdgeView(gv, ev1.netView!!)
		ev2.addSegmentPoint(Point2D(200, 0))
		ev2.addSegmentPoint(Point2D(100, 0))
		gv.add(ev2)

		val vv1 = TestVerticeView()
		ev1.connectToOrigin(Connection(vv1, vv1.model.getOutput()))

		val vv2 = TestVerticeView()
		ev2.connectToOrigin(Connection(vv2, vv2.model.getOutput()))

		EdgeViewSplitterJoiner.join(ev1, ev2)

		assertEquals(2, ev1.segmentPointCount)
		assertEquals(Point2D(0, 0), ev1.getSegmentPoint(0))
		assertEquals(Point2D(200, 0), ev1.getSegmentPoint(1))

		assertEquals(vv2, ev1.destination!!.connectableView as TestVerticeView)
		assertEquals(vv1, ev1.origin!!.connectableView as TestVerticeView)
	}
}