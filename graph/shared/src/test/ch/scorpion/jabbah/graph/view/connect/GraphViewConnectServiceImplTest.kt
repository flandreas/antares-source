package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

/**
 * Unit tests for [GraphViewConnectServiceImpl].
 */
class GraphViewConnectServiceImplTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val service = GraphViewModule.graphViewConnectService
	private val edgeViewFactory = GraphViewModule.getEdgeViewFactory<Boolean>()
	private val gv = GraphViewModule.createGraphView<GraphElementView<*>>()

	@BeforeTest
	fun setup() {
		TestTranslationsBuilder().withAnyKey()
	}

	@Test
	fun shouldConnect() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
		gv.add(vv1).add(vv2).add(vv3)

		val ev = service.addConnection<Boolean>(gv, vv1, vv2)

		// Model assertions
		assertTrue(ev.model!!.isConnectedWith(vv1.model!!.getOutput()))
		assertFalse(ev.model!!.isConnectedWith(vv1.model!!.getInput()))
		assertTrue(ev.model!!.isConnectedWith(vv2.model!!.getInput()))
		assertFalse(ev.model!!.isConnectedWith(vv2.model!!.getOutput()))

		// View assertions
		assertTrue(gv.contains(ev))
		assertSame(vv1, ev.origin as TestVerticeView)
		assertSame(vv2, ev.destination as TestVerticeView)

		// Assert orthogonal layout
		assertEquals(4, ev.segmentPointCount)
		assertEquals(Point2D(100, 100), ev.getSegmentPoint(0))
		assertEquals(Point2D(150, 100), ev.getSegmentPoint(1))
		assertEquals(Point2D(150, 200), ev.getSegmentPoint(2))
		assertEquals(Point2D(200, 200), ev.getSegmentPoint(3))
	}

	@Test
	fun shouldUnconnectVerticeView() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
		gv.add(vv1).add(vv2).add(vv3)

		val ev = service.addConnection<Boolean>(gv, vv1, vv2)

		service.unconnect(gv, vv2)

		assertNull(ev.destination)
		assertNull(vv2.getPort(1)?.net)
	}

	@Test
	fun shouldUnconnectOpenBeginEdgeView() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
		gv.add(vv1).add(vv2).add(vv3)

		val ev = edgeViewFactory.createEdgeView()
		ev.addSegmentPoint(Point2D(100, 100))
		ev.addSegmentPoint(Point2D(200, 100))
		gv.add(ev)
		service.connectToDestination(ev, vv1, vv1.model!!.getInput())

		service.unconnect(ev)
		assertNull(ev.destination)
		assertNull(ev.destinationPort)
	}

	@Test
	fun shouldUnconnectOpenDestinationEdgeView() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
		gv.add(vv1).add(vv2).add(vv3)

		val ev = edgeViewFactory.createEdgeView()
		ev.addSegmentPoint(Point2D(100, 100))
		ev.addSegmentPoint(Point2D(200, 100))
		gv.add(ev)
		service.connectToOrigin(ev, vv1, vv1.model!!.getOutput())

		service.unconnect(ev)

		assertNull(ev.origin)
		assertNull(ev.originPort)
	}

	@Test
	fun shouldSplit() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100))
		val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
		gv.add(vv1).add(vv2).add(vv3)

		val ev1 = service.addConnection<Boolean>(gv, vv1, vv2)
		val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
		ev2.addSegmentPoint(Point2D(150, 100))

		val result = splitToInput(ev1, ev2, vv3)

		// Model assertions
		assertEquals(4, gv.graph!!.elementsCount)
		assertSame(ev1.model, result.tailEdgeView.model)
		assertSame(ev1.model, result.nodeView.model)

		assertTrue(ev1.model!!.isConnectedWith(vv1.model!!.getOutput()))
		assertTrue(ev1.model!!.isConnectedWith(vv2.model!!.getInput()))
		assertTrue(ev1.model!!.isConnectedWith(vv3.model!!.getInput()))

		// View assertions
		assertEquals(7, gv.drawablesCount)
		assertSame(vv1, ev1.origin as TestVerticeView)
		assertSame(result.nodeView, ev1.destination as NodeView<Boolean>)
		assertEquals(result.nodeView.location, ev1.getSegmentPoint(ev1.segmentPointCount - 1))

		assertSame(result.nodeView, ev2.origin as NodeView<Boolean>)
		assertNotNull(result.nodeView.parent)

		assertEquals(result.nodeView, result.tailEdgeView.origin as NodeView<Boolean>)
		assertSame(vv2, result.tailEdgeView.destination as TestVerticeView)
		assertEquals(result.nodeView.location, result.tailEdgeView.getSegmentPoint(0))
	}

	/** Split an [EdgeView] that connects two [InputPort]s (which can result from deleting segments).*/
	@Test
	fun shouldSplitInIn() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100), inputDirection = Direction.EAST)
		val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
		gv.add(vv1).add(vv2).add(vv3)

		val ev1 = service.addConnection(
			gv,
			vv1.getPortView(vv1.model!!.getInput<Boolean>())!!,
			vv2.getPortView(vv2.model!!.getInput())!!)
		val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
		ev2.addSegmentPoint(Point2D(150, 100))

		val result = splitToOutput(ev1, ev2, vv3)

		// Model assertions
		assertEquals(4, gv.graph!!.elementsCount)
		assertSame(ev1.model, result.tailEdgeView.model)
		assertSame(ev1.model, result.nodeView.model)
		assertSame(ev1.model, ev2.model)

		assertTrue(ev1.model!!.isConnectedWith(vv1.model!!.getInput()))
		assertTrue(ev1.model!!.isConnectedWith(vv2.model!!.getInput()))
		assertTrue(ev1.model!!.isConnectedWith(vv3.model!!.getOutput()))

		// View assertions
		assertEquals(7, gv.drawablesCount)

		assertSame(result.nodeView, ev1.origin as NodeView<Boolean>)
		assertSame(vv1, ev1.destination as TestVerticeView)
		assertEquals(result.nodeView.location, ev1.getSegmentPoint(0))

		assertSame(vv3, ev2.origin as TestVerticeView)
		assertSame(result.nodeView, ev2.destination as NodeView<Boolean>)

		assertEquals(result.nodeView, result.tailEdgeView.origin as NodeView<Boolean>)
		assertEquals(vv2, result.tailEdgeView.destination as TestVerticeView)
		assertEquals(result.nodeView.location, result.tailEdgeView.getSegmentPoint(0))
	}

	/** Regression test for a bug that occurred on 02.10.17.*/
	@Test
	fun shouldSupportMultipleSplit() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(0, 100), outputDirection = Direction.EAST)
		val vv2 = TestVerticeView(name = "2", loc = Point2D(100, 0), inputDirection = Direction.SOUTH)
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 0), inputDirection = Direction.SOUTH)
		val vv4 = TestVerticeView(name = "3", loc = Point2D(300, 0), inputDirection = Direction.SOUTH)
		gv.add(vv1).add(vv2).add(vv3).add(vv4)

		val ev1 = service.addConnection<Boolean>(gv, vv1, vv2)

		val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
		ev2.addSegmentPoint(Point2D(100, 100))
		val result1 = splitToInput(ev1, ev2, vv3)

		val ev3 = edgeViewFactory.createEdgeView(ev1.model!!)
		ev3.addSegmentPoint(Point2D(200, 100))
		val result2 = splitToInput(ev2, ev3, vv4)

		assertEquals(Point2D(0, 100), ev1.getSegmentPoint(0))
		assertEquals(Point2D(100, 100), ev1.getSegmentPoint(1))

		assertEquals(Point2D(100, 100), result1.tailEdgeView.getSegmentPoint(0))
		assertEquals(Point2D(100, 0), result1.tailEdgeView.getSegmentPoint(1))
		assertEquals(Point2D(100, 100), result1.nodeView.location)
		assertEquals(Point2D(100, 100), result1.newEdgeView.getSegmentPoint(0))
		assertEquals(Point2D(200, 100), result1.newEdgeView.getSegmentPoint(1))

		assertEquals(Point2D(200, 100), result2.tailEdgeView.getSegmentPoint(0))
		assertEquals(Point2D(200, 0), result2.tailEdgeView.getSegmentPoint(1))
		assertEquals(Point2D(200, 100), result2.nodeView.location)
		assertEquals(Point2D(200, 100), result2.newEdgeView.getSegmentPoint(0))
		assertEquals(Point2D(300, 100), result2.newEdgeView.getSegmentPoint(1))
		assertEquals(Point2D(300, 0), result2.newEdgeView.getSegmentPoint(2))
	}

	@Test
	fun shouldRemoveNodeView() {
		val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100), inputDirection = Direction.EAST)
		val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
		val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
		gv.add(vv1).add(vv2).add(vv3)

		val ev1 = service.addConnection<Boolean>(gv, vv1, vv2)
		val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
		gv.add(ev2)
		ev2.addSegmentPoint(Point2D(150, 100))
		val result = splitToInput(ev1, ev2, vv3)
		ev2.connectToOrigin(null, null)

		val remainingEV = service.removeNodeView(gv, result.nodeView)

		// Model assertions
		assertTrue(remainingEV.model!!.isConnectedWith(vv1.model!!.getOutput()))

		// View assertions
		assertEquals(0, result.nodeView.getOutgoingEdgeViews().size)
		assertFalse(gv.contains(result.nodeView))
	}

	private fun splitToInput(
		splittedEdgeView: EdgeView<Boolean>,
		newEdgeView: EdgeView<Boolean>,
		vv: TestVerticeView
	): SplitEdgeViewResult<Boolean> {
		val inputPort = vv.model!!.getInput<Boolean>()
		return service.split(gv, splittedEdgeView, 1, newEdgeView, vv.getPortView(inputPort))
	}

	private fun splitToOutput(
		splittedEdgeView: EdgeView<Boolean>,
		newEdgeView: EdgeView<Boolean>,
		vv: TestVerticeView
	): SplitEdgeViewResult<Boolean> {
		val outputPort = vv.model!!.getOutput<Boolean>()
		return service.split(gv, splittedEdgeView, 1, newEdgeView, vv.getPortView(outputPort))
	}
}