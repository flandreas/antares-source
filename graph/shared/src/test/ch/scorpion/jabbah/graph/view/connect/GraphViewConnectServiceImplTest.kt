package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.draw.style.DrawStyleModule
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
    private lateinit var vv1: TestVerticeView
    private lateinit var vv2: TestVerticeView
    private lateinit var vv3: TestVerticeView

    @Before
    fun setup() {
        TestTranslationsBuilder()
            .withResource("test.name")
            .withResource("graph.name.unknown")
            .withResource("test.desc")
        vv1 = TestVerticeView(DrawStyleModule.styleProvider)
        vv2 = TestVerticeView(DrawStyleModule.styleProvider)
        vv3 = TestVerticeView(DrawStyleModule.styleProvider)
        vv1.location = Point2D(100, 100)
        vv2.location = Point2D(200, 200)
        vv3.location = Point2D(200, 100)
        gv.add(vv1).add(vv2).add(vv3)
    }

    @Test
    fun shouldConnect() {
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
        val ev = service.addConnection<Boolean>(gv, vv1, vv2)

        service.unconnect(gv, vv2)

        assertThat(ev.destination, `is`(nullValue()))
        assertThat(vv2.getPort(1).net, `is`(nullValue()))
    }

    @Test
    fun shouldSplit() {
        val ev1 = service.addConnection<Boolean>(gv, vv1, vv2)
        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        ev2.addSegmentPoint(Point2D(150, 100))

        val result = split(ev1, ev2)

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

    @Test
    fun shouldRemoveNodeView() {
        val ev1 = service.addConnection<Boolean>(gv, vv1, vv2)
        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        gv.add(ev2)
        ev2.addSegmentPoint(Point2D(150, 100))
        val result = split(ev1, ev2)
        ev2.connectToOrigin(null, null)

        val remainingEV = service.removeNodeView(gv, result.nodeView)

        // Model assertions
        assertThat(remainingEV.model!!.isConnectedWith(vv1.model!!.getOutput()), `is`(true))

        // View assertions
        assertThat(result.nodeView.getOutgoingEdgeViews().size, `is`(0))
        assertThat(gv.contains(result.nodeView), `is`(false))
    }

    private fun split(splittedEdgeView: EdgeView<Boolean>, newEdgeView: EdgeView<Boolean>): SplitEdgeViewResult<Boolean> {
        val vv3InputPort = vv3.model!!.getInput<Boolean>()
        return service.split(gv, splittedEdgeView, 1, newEdgeView, vv3.getPortView(vv3InputPort))
    }
}