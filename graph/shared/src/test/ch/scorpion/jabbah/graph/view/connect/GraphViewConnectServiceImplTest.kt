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
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [GraphViewConnectServiceImpl].
 */
class GraphViewConnectServiceImplTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private val service = GraphViewModule.graphViewConnectService
    private val edgeViewFactory = GraphViewModule.getEdgeViewFactory<Boolean>()
    private val gv = GraphViewModule.createGraphView<GraphElementView<*>>()

    @Before
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
        assertThat(ev.model!!.isConnectedWith(vv1.model!!.getOutput()), `is`(true))
        assertThat(ev.model!!.isConnectedWith(vv1.model!!.getInput()), `is`(false))
        assertThat(ev.model!!.isConnectedWith(vv2.model!!.getInput()), `is`(true))
        assertThat(ev.model!!.isConnectedWith(vv2.model!!.getOutput()), `is`(false))

        // View assertions
        assertThat(gv.contains(ev), `is`(true))
        assertThat(ev.origin as TestVerticeView, `is`(sameInstance(vv1)))
        assertThat(ev.destination as TestVerticeView, `is`(sameInstance(vv2)))

        // Assert orthogonal layout
        assertThat(ev.segmentPointCount, `is`(4))
        assertThat(ev.getSegmentPoint(0), `is`(Point2D(100, 100)))
        assertThat(ev.getSegmentPoint(1), `is`(Point2D(150, 100)))
        assertThat(ev.getSegmentPoint(2), `is`(Point2D(150, 200)))
        assertThat(ev.getSegmentPoint(3), `is`(Point2D(200, 200)))
    }

    @Test
    fun shouldUnconnectVerticeView() {
        val vv1 = TestVerticeView(name = "1", loc = Point2D(100, 100))
        val vv2 = TestVerticeView(name = "2", loc = Point2D(200, 200))
        val vv3 = TestVerticeView(name = "3", loc = Point2D(200, 100))
        gv.add(vv1).add(vv2).add(vv3)

        val ev = service.addConnection<Boolean>(gv, vv1, vv2)

        service.unconnect(gv, vv2)

        assertThat(ev.destination, `is`(nullValue()))
        assertThat(vv2.getPort(1)?.net, `is`(nullValue()))
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
        assertThat(ev.destination, `is`(nullValue()))
        assertThat(ev.destinationPort, `is`(nullValue()))
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

        assertThat(ev.origin, `is`(nullValue()))
        assertThat(ev.originPort, `is`(nullValue()))
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
        assertThat(gv.graph!!.elementsCount, `is`(4))
        assertThat(ev1.model, `is`(sameInstance(result.tailEdgeView.model)))
        assertThat(ev1.model, `is`(sameInstance(result.nodeView.model)))

        assertThat(ev1.model!!.isConnectedWith(vv1.model!!.getOutput()), `is`(true))
        assertThat(ev1.model!!.isConnectedWith(vv2.model!!.getInput()), `is`(true))
        assertThat(ev1.model!!.isConnectedWith(vv3.model!!.getInput()), `is`(true))

        // View assertions
        assertThat(gv.drawablesCount, `is`(7))
        assertThat(ev1.origin as TestVerticeView, `is`(sameInstance(vv1)))
        assertThat(ev1.destination as NodeView<Boolean>, `is`(sameInstance(result.nodeView)))
        assertThat(ev1.getSegmentPoint(ev1.segmentPointCount - 1), `is`(result.nodeView.location))

        assertThat(ev2.origin as NodeView<Boolean>, `is`(sameInstance(result.nodeView)))
        assertThat(result.nodeView.parent, `is`(notNullValue()))

        assertThat(result.tailEdgeView.origin as NodeView<Boolean>, `is`(result.nodeView))
        assertThat(result.tailEdgeView.destination as TestVerticeView, `is`(sameInstance(vv2)))
        assertThat(result.tailEdgeView.getSegmentPoint(0), `is`(result.nodeView.location))
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
                vv2.getPortView(vv2.model!!.getInput<Boolean>())!!)
        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        ev2.addSegmentPoint(Point2D(150, 100))

        val result = splitToOutput(ev1, ev2, vv3)

        // Model assertions
        assertThat(gv.graph!!.elementsCount, `is`(4))
        assertThat(ev1.model, `is`(sameInstance(result.tailEdgeView.model)))
        assertThat(ev1.model, `is`(sameInstance(result.nodeView.model)))
        assertThat(ev1.model, `is`(sameInstance(ev2.model)))

        assertThat(ev1.model!!.isConnectedWith(vv1.model!!.getInput()), `is`(true))
        assertThat(ev1.model!!.isConnectedWith(vv2.model!!.getInput()), `is`(true))
        assertThat(ev1.model!!.isConnectedWith(vv3.model!!.getOutput()), `is`(true))

        // View assertions
        assertThat(gv.drawablesCount, `is`(7))

        assertThat(ev1.origin as NodeView<Boolean>, `is`(sameInstance(result.nodeView)))
        assertThat(ev1.destination as TestVerticeView, `is`(sameInstance(vv1)))
        assertThat(ev1.getSegmentPoint(0), `is`(result.nodeView.location))

        assertThat(ev2.origin as TestVerticeView, `is`(sameInstance(vv3)))
        assertThat(ev2.destination as NodeView<Boolean>, `is`(sameInstance(result.nodeView)))

        assertThat(result.tailEdgeView.origin as NodeView<Boolean>, `is`(result.nodeView))
        assertThat(result.tailEdgeView.destination as TestVerticeView, `is`(vv2))
        assertThat(result.tailEdgeView.getSegmentPoint(0), `is`(result.nodeView.location))
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

        assertThat(ev1.getSegmentPoint(0), `is`(Point2D(0, 100)))
        assertThat(ev1.getSegmentPoint(1), `is`(Point2D(100, 100)))

        assertThat(result1.tailEdgeView.getSegmentPoint(0), `is`(Point2D(100, 100)))
        assertThat(result1.tailEdgeView.getSegmentPoint(1), `is`(Point2D(100, 0)))
        assertThat(result1.nodeView.location, `is`(Point2D(100, 100)))
        assertThat(result1.newEdgeView.getSegmentPoint(0), `is`(Point2D(100, 100)))
        assertThat(result1.newEdgeView.getSegmentPoint(1), `is`(Point2D(200, 100)))

        assertThat(result2.tailEdgeView.getSegmentPoint(0), `is`(Point2D(200, 100)))
        assertThat(result2.tailEdgeView.getSegmentPoint(1), `is`(Point2D(200, 0)))
        assertThat(result2.nodeView.location, `is`(Point2D(200, 100)))
        assertThat(result2.newEdgeView.getSegmentPoint(0), `is`(Point2D(200, 100)))
        assertThat(result2.newEdgeView.getSegmentPoint(1), `is`(Point2D(300, 100)))
        assertThat(result2.newEdgeView.getSegmentPoint(2), `is`(Point2D(300, 0)))
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
        assertThat(remainingEV.model!!.isConnectedWith(vv1.model!!.getOutput()), `is`(true))

        // View assertions
        assertThat(result.nodeView.getOutgoingEdgeViews().size, `is`(0))
        assertThat(gv.contains(result.nodeView), `is`(false))
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