package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test


/**
 * Unit tests for [EdgeViewImpl].
 */
class EdgeViewImplTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private val edgeViewFactory = GraphViewModule.getEdgeViewFactory<Boolean>()
    private val graphView = GraphViewModule.createGraphView<GraphElementView<*>>()

    @Before
    fun setup() {
        TestTranslationsBuilder()
                .withResource("test.name")
                .withResource("graph.name.unknown")
                .withResource("test.desc")
    }

    @Test
    fun shouldUpdateBoundingBox() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(100, 100))
        ev.addSegmentPoint(Point2D(200, 100))
        ev.addSegmentPoint(Point2D(200, 200))
        // Result includes line width and EdgeEndpointViews
        assertThat(ev.boundingBox as Rectangle2D, `is`(Rectangle2D(92, 92, 116, 116)))
    }

    @Test
    fun regularEdgeViewShouldNotBeDegenerated() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))
        assertThat(ev.isDegenerated, `is`(false))
    }

    @Test
    fun shouldDetermineEmptyDegeneration() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(10, 10))
        assertThat(ev.isDegenerated, `is`(true))
    }

    @Test
    fun shouldDetermineOriginDegeneration() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))
        assertThat(ev.isDegenerated, `is`(true))
    }

    @Test
    fun shouldDetermineDestinationDegeneration() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))
        ev.addSegmentPoint(Point2D(20, 10))
        assertThat(ev.isDegenerated, `is`(true))
    }

    @Test
    fun shouldCompactEqualPoints() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(10, 10))
        ev.addSegmentPoint(Point2D(20, 10))

        ev.compact()

        assertThat(ev.segmentPointCount, `is`(2))
    }

    @Test
    fun shouldCompactHorizontally() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(10, 0))
        ev.addSegmentPoint(Point2D(20, 0))

        ev.compact()

        assertThat(ev.segmentPointCount, `is`(2))
    }

    @Test
    fun shouldCompactVertically() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(0, 10))
        ev.addSegmentPoint(Point2D(0, 20))

        ev.compact()

        assertThat(ev.segmentPointCount, `is`(2))
    }

    @Test
    fun shouldCalculateLengthWithoutPoints() {
        val ev = edgeViewFactory.createEdgeView()
        assertThat(ev.length, `is`(0.0))
    }

    @Test
    fun shouldCalculateLengthWithOnePoint() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        assertThat(ev.length, `is`(0.0))
    }

    @Test
    fun shouldCalculateLength() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(0, 10))
        ev.addSegmentPoint(Point2D(20, 10))

        assertThat(ev.length, `is`(30.0))
    }

    @Test
    fun shouldSplitInMiddleOfSegment() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        ev.addSegmentPoint(Point2D(100, 50))
        ev.addSegmentPoint(Point2D(200, 50))

        val newEV = ev.split(1, Point2D(100, 25)) { edgeViewFactory.createEdgeView(it as Net<Boolean>) }

        assertThat(ev.segmentPointCount, `is`(3))
        assertThat(ev.getSegmentPoint(0), `is`(Point2D(0, 0)))
        assertThat(ev.getSegmentPoint(1), `is`(Point2D(100, 0)))
        assertThat(ev.getSegmentPoint(2), `is`(Point2D(100, 25)))

        assertThat(newEV.segmentPointCount, `is`(3))
        assertThat(newEV.getSegmentPoint(0), `is`(Point2D(100, 25)))
        assertThat(newEV.getSegmentPoint(1), `is`(Point2D(100, 50)))
        assertThat(newEV.getSegmentPoint(2), `is`(Point2D(200, 50)))
    }

    @Test
    fun shouldSplitAtStartOfSegment() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        ev.addSegmentPoint(Point2D(100, 50))
        ev.addSegmentPoint(Point2D(200, 50))

        val newEV = ev.split(1, Point2D(100, 0)) { edgeViewFactory.createEdgeView(it as Net<Boolean>) }

        assertThat(ev.segmentPointCount, `is`(2))
        assertThat(ev.getSegmentPoint(0), `is`(Point2D(0, 0)))
        assertThat(ev.getSegmentPoint(1), `is`(Point2D(100, 0)))

        assertThat(newEV.segmentPointCount, `is`(3))
        assertThat(newEV.getSegmentPoint(0), `is`(Point2D(100, 0)))
        assertThat(newEV.getSegmentPoint(1), `is`(Point2D(100, 50)))
        assertThat(newEV.getSegmentPoint(2), `is`(Point2D(200, 50)))
    }

    @Test
    fun shouldJoinTail() {
        val ev1 = edgeViewFactory.createEdgeView()
        ev1.addSegmentPoint(Point2D(0, 0))
        ev1.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev1)

        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        ev2.addSegmentPoint(Point2D(100, 0))
        ev2.addSegmentPoint(Point2D(200, 0))
        graphView.add(ev2)

        val vv = TestVerticeView(DrawStyleModule.styleProvider)
        ev2.connectToDestination(vv, vv.model!!.getInput<Boolean>())

        ev1.join(ev2)

        assertThat(ev1.segmentPointCount, `is`(2))
        assertThat(ev1.getSegmentPoint(0), `is`(Point2D(0, 0)))
        assertThat(ev1.getSegmentPoint(1), `is`(Point2D(200, 0)))

        assertThat(ev1.destination as TestVerticeView, `is`(vv))
        assertThat(ev2.destination, `is`(nullValue()))
    }

    @Test
    fun shouldJoinHead() {
        val ev1 = edgeViewFactory.createEdgeView()
        ev1.addSegmentPoint(Point2D(0, 0))
        ev1.addSegmentPoint(Point2D(100, 0))
        graphView.add(ev1)

        val ev2 = edgeViewFactory.createEdgeView(ev1.model!!)
        ev2.addSegmentPoint(Point2D(100, 0))
        ev2.addSegmentPoint(Point2D(200, 0))
        graphView.add(ev2)

        val vv = TestVerticeView(DrawStyleModule.styleProvider)
        ev1.connectToOrigin(vv, vv.model!!.getOutput<Boolean>())

        ev2.join(ev1)

        assertThat(ev2.segmentPointCount, `is`(2))
        assertThat(ev2.getSegmentPoint(0), `is`(Point2D(0, 0)))
        assertThat(ev2.getSegmentPoint(1), `is`(Point2D(200, 0)))

        assertThat(ev2.origin as TestVerticeView, `is`(vv))
        assertThat(ev1.origin, `is`(nullValue()))
    }

    @Test
    fun shouldCalculateSegmentDirection() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(0, 0))
        ev.addSegmentPoint(Point2D(100, 0))
        ev.addSegmentPoint(Point2D(100, 100))
        ev.addSegmentPoint(Point2D(0, 100))
        ev.addSegmentPoint(Point2D(0, 0))

        assertThat(ev.getSegmentDirection(0)!!, `is`(Direction.EAST))
        assertThat(ev.getSegmentDirection(1)!!, `is`(Direction.SOUTH))
        assertThat(ev.getSegmentDirection(2)!!, `is`(Direction.WEST))
        assertThat(ev.getSegmentDirection(3)!!, `is`(Direction.NORTH))
    }

    @Test
    fun shouldFindSegmentWithMinimalArea() {
        val ev = edgeViewFactory.createEdgeView()
        ev.addSegmentPoint(Point2D(150, 100))
        ev.addSegmentPoint(Point2D(150, 0))
        ev.addSegmentPoint(Point2D(200, 0))

        assertThat(ev.findSegment(150.0, 50.0, 1), `is`(0))
    }
}