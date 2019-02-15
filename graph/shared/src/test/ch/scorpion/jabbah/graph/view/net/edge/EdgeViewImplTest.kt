package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeViewConnectionState
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

/**
 * Unit tests for [EdgeViewImpl].
 */
class EdgeViewImplTest {

    companion object {
	    init {
	    	GraphViewTestRule.configure()
	    }
    }

    private val edgeViewFactory = GraphViewModule.getEdgeViewFactory<Boolean>()
    private val graphView = GraphViewModule.createGraphView<GraphElementView<*>>()
	
    @Test
    fun shouldUpdateBoundingBox() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(100, 100))
        ev.addSegmentPoint(Point2D(200, 100))
        ev.addSegmentPoint(Point2D(200, 200))
        // Result includes line width and EdgeEndpointViews
        assertEquals(Rectangle2D(92, 92, 116, 116), ev.boundingBox as Rectangle2D)
    }

    @Test
    fun regularEdgeViewShouldNotBeDegenerated() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))
        assertFalse(ev.isDegenerated)
    }

    @Test
    fun shouldDetermineEmptyDegeneration() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(10, 10))
        assertTrue(ev.isDegenerated)
    }

    @Test
    fun shouldDetermineOriginDegeneration() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))
	    assertTrue(ev.isDegenerated)
    }

    @Test
    fun shouldDetermineDestinationDegeneration() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))
        ev.addSegmentPoint(Point2D(20, 10))
	    assertTrue(ev.isDegenerated)
    }

    @Test
    fun shouldCompactEqualPoints() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))

        ev.compact()

        assertEquals(2, ev.segmentPointCount)
    }

    @Test
    fun shouldCompactHorizontally() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(10, 0))
        ev.addSegmentPoint(Point2D(20, 0))

        ev.compact()

        assertEquals(2, ev.segmentPointCount)
    }

    @Test
    fun shouldCompactVertically() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(0, 10))
        ev.addSegmentPoint(Point2D(0, 20))

        ev.compact()

        assertEquals(2, ev.segmentPointCount)
    }

    @Test
    fun shouldCalculateLengthWithoutPoints() {
        val ev = edgeViewFactory.createEdgeView()
        assertEquals(0.0, ev.length)
    }

    @Test
    fun shouldCalculateLengthWithOnePoint() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        assertEquals(0.0, ev.length)
    }

    @Test
    fun shouldCalculateLength() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(0, 10))
        ev.addSegmentPoint(Point2D(20, 10))

        assertEquals(30.0, ev.length)
    }

    @Test
    fun shouldSplitInMiddleOfSegment() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        ev.addSegmentPoint(Point2D(100, 50))
        ev.addSegmentPoint(Point2D(200, 50))

        val newEV = ev.split(1, Point2D(100, 25)) { edgeViewFactory.createEdgeView(it as Net<Boolean>) }

        assertEquals(3, ev.segmentPointCount)
        assertEquals(Point2D(0, 0), ev.getSegmentPoint(0))
        assertEquals(Point2D(100, 0), ev.getSegmentPoint(1))
        assertEquals(Point2D(100, 25), ev.getSegmentPoint(2))

        assertEquals(3, newEV.segmentPointCount)
        assertEquals(Point2D(100, 25), newEV.getSegmentPoint(0))
        assertEquals(Point2D(100, 50), newEV.getSegmentPoint(1))
        assertEquals(Point2D(200, 50),newEV.getSegmentPoint(2))
    }

    @Test
    fun shouldSplitAtStartOfSegment() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        ev.addSegmentPoint(Point2D(100, 50))
        ev.addSegmentPoint(Point2D(200, 50))

        val newEV = ev.split(1, Point2D(100, 0)) { edgeViewFactory.createEdgeView(it as Net<Boolean>) }

        assertEquals(2, ev.segmentPointCount)
        assertEquals(Point2D(0, 0), ev.getSegmentPoint(0))
        assertEquals(Point2D(100, 0), ev.getSegmentPoint(1))

        assertEquals(3, newEV.segmentPointCount)
        assertEquals(Point2D(100, 0), newEV.getSegmentPoint(0))
        assertEquals(Point2D(100, 50), newEV.getSegmentPoint(1))
        assertEquals(Point2D(200, 50), newEV.getSegmentPoint(2))
    }

    @Test
    fun shouldJoinOtherHeadWithTail() {
        val ev1 = edgeViewFactory.createEdgeView()
        ev1.addSegmentPoint(Point2D(0, 0))
        ev1.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev1)

        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        ev2.addSegmentPoint(Point2D(100, 0))
        ev2.addSegmentPoint(Point2D(200, 0))
        graphView.add(ev2)

        val vv = TestVerticeView()
        ev2.connectToDestination(vv, vv.model!!.getInput())

        ev1.join(ev2)

        assertEquals(2, ev1.segmentPointCount)
        assertEquals(Point2D(0, 0), ev1.getSegmentPoint(0))
        assertEquals(Point2D(200, 0), ev1.getSegmentPoint(1))

        assertEquals(ev1.destination as TestVerticeView, vv)
        assertNull(ev2.destination)
    }

    @Test
    fun shouldJoinOtherTailWithHead() {
        val ev1 = edgeViewFactory.createEdgeView()
        ev1.addSegmentPoint(Point2D(0, 0))
        ev1.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev1)

        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        ev2.addSegmentPoint(Point2D(100, 0))
        ev2.addSegmentPoint(Point2D(200, 0))
        graphView.add(ev2)

        val vv = TestVerticeView()
        ev1.connectToOrigin(vv, vv.model!!.getOutput())

        ev2.join(ev1)

        assertEquals(2, ev2.segmentPointCount)
        assertEquals(Point2D(0, 0), ev2.getSegmentPoint(0))
        assertEquals(Point2D(200, 0), ev2.getSegmentPoint(1))

        assertEquals(vv, ev2.origin as TestVerticeView)
        assertNull(ev1.origin)
    }

    @Test
    fun shouldJoinOtherHeadWithHead() {
        val ev1 = edgeViewFactory.createEdgeView()
        ev1.addSegmentPoint(Point2D(0, 0))
        ev1.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev1)

        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        ev2.addSegmentPoint(Point2D(0, 0))
        ev2.addSegmentPoint(Point2D(-100, 0))
        graphView.add(ev2)

        val vv1 = TestVerticeView()
        ev1.connectToDestination(vv1, vv1.model!!.getOutput())

        val vv2 = TestVerticeView()
        ev2.connectToDestination(vv2, vv2.model!!.getOutput())

        ev1.join(ev2)

        assertEquals(2, ev1.segmentPointCount)
        assertEquals(Point2D(-100, 0), ev1.getSegmentPoint(0))
        assertEquals(Point2D(100, 0), ev1.getSegmentPoint(1))

        assertEquals(vv2, ev1.origin as TestVerticeView)
        assertEquals(vv1, ev1.destination as TestVerticeView)
    }

    @Test
    fun shouldCalculateSegmentDirection() {
        val ev = edgeViewFactory.createEdgeView()
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
    fun shouldFindSegmentWithMinimalArea() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(150, 100))
        ev.addSegmentPoint(Point2D(150, 0))
        ev.addSegmentPoint(Point2D(200, 0))

        assertEquals(0, ev.findSegment(150.0, 50.0, 1))
    }

    @Test
    fun shouldDetectUnconnectedConnectionState() {
        val ev = edgeViewFactory.createEdgeView()
        assertEquals(ev.connectionState, EdgeViewConnectionState.Unconnected)
    }

    @Test
    fun shouldDetectInputConnectionState() {
        val ev = edgeViewFactory.createEdgeView()
        val vv = TestVerticeView()
        ev.connectToDestination(vv, vv.model!!.getInput())
        assertEquals(ev.connectionState, EdgeViewConnectionState.Input)
    }

    @Test
    fun shouldDetectOutputConnectionState() {
        val ev = edgeViewFactory.createEdgeView()
        val vv = TestVerticeView()
        ev.connectToDestination(vv, vv.model!!.getOutput())
        assertEquals(ev.connectionState, EdgeViewConnectionState.Output)
    }

    @Test
    fun shouldDetectInputOutputConnectionState() {
        val ev = edgeViewFactory.createEdgeView()
        val vv1 = TestVerticeView()
        val vv2 = TestVerticeView()
        ev.connectToOrigin(vv1, vv1.model!!.getOutput())
        ev.connectToDestination(vv2, vv2.model!!.getInput())
        assertEquals(ev.connectionState, EdgeViewConnectionState.InputOutput)
    }

    @Test
    fun shouldDetectInputInputConnectionState() {
        val ev = edgeViewFactory.createEdgeView()
        val vv1 = TestVerticeView()
        val vv2 = TestVerticeView()
        ev.connectToOrigin(vv1, vv1.model!!.getInput())
        ev.connectToDestination(vv2, vv2.model!!.getInput())
        assertEquals(ev.connectionState, EdgeViewConnectionState.InputInput)
    }

    @Test
    fun shouldDetectOutputOutputConnectionState() {
        val ev = edgeViewFactory.createEdgeView()
        val vv1 = TestVerticeView()
        val vv2 = TestVerticeView()
        ev.connectToOrigin(vv1, vv1.model!!.getOutput())
        ev.connectToDestination(vv2, vv2.model!!.getOutput())
        assertEquals(ev.connectionState, EdgeViewConnectionState.OutputOutput)
    }

    @Test
    fun shouldNotMoveIndividually() {
        val vv1 = TestVerticeView(loc = Point2D(0, 0), outputDirection = Direction.EAST)
        val vv2 = TestVerticeView(loc = Point2D(100, 0), inputDirection = Direction.WEST)
        graphView.add(vv1)
        graphView.add(vv2)
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev)
        ev.connectToOrigin(vv1, vv1.model!!.getOutput())
        ev.connectToDestination(vv2, vv2.model!!.getOutput())

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
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev)
        ev.connectToOrigin(vv1, vv1.model!!.getOutput())
        ev.connectToDestination(vv2, vv2.model!!.getOutput())

        ev.prepareMoveBy(listOf(vv1, vv2, ev))
        ev.moveBy(0.0, 50.0)

        assertEquals(Point2D(0, 50), ev.getSegmentPoint(0))
        assertEquals(Point2D(100, 50), ev.getSegmentPoint(1))
    }

    @Test
    fun shouldMoveUnaryConnected() {
        val vv1 = TestVerticeView(loc = Point2D(0, 0), outputDirection = Direction.EAST)
        graphView.add(vv1)
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev)
        ev.connectToOrigin(vv1, vv1.model!!.getOutput())

        ev.prepareMoveBy(listOf(vv1, ev))
        ev.moveBy(0.0, 50.0)

        assertEquals(Point2D(0, 50), ev.getSegmentPoint(0))
        assertEquals(Point2D(100, 50), ev.getSegmentPoint(1))
    }
}