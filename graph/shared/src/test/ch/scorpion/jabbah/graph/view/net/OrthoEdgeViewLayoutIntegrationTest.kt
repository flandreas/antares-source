package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.net.edge.Layout
import ch.scorpion.jabbah.graph.view.net.edge.OrthoEdgeViewLayout
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Integration tests for integrating [OrthoEdgeViewLayout] and [NodeViewImpl].
 */
class OrthoEdgeViewLayoutIntegrationTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private lateinit var builder: GraphViewBuilder<Boolean>

    @Before
    fun setup(){
        TestTranslationsBuilder().withAnyKey()
        builder = GraphViewBuilder()
    }

    @Test
    fun shouldSplitHorizontalEdgeView() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVertice(createVerticeView(200, 200, Direction.WEST))
        val origEdgeView = builder.connect(v1, v2)

        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

        assertThat(splitResult.tailEdgeView.segmentPointCount, `is`(2))
        assertThat(splitResult.tailEdgeView.isAdjusted, `is`(false))

        assertThat(splitResult.newEdgeView.segmentPointCount, `is`(3))
        assertThat(splitResult.newEdgeView.isAdjusted, `is`(false))

        assertThat(origEdgeView.segmentPointCount, `is`(2))
        assertThat(origEdgeView.isAdjusted, `is`(false))
    }

    @Test
    fun shouldSplitEdgeViewAtCorner() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVertice(createVerticeView(200, 200, Direction.WEST))
        val origEdgeView = builder.connect(v1, v3)

        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v2)

        assertThat(splitResult.tailEdgeView.segmentPointCount, `is`(3))
        assertThat(splitResult.tailEdgeView.isAdjusted, `is`(false))

        assertThat(splitResult.newEdgeView.segmentPointCount, `is`(2))
        assertThat(splitResult.newEdgeView.isAdjusted, `is`(false))

        assertThat(origEdgeView.segmentPointCount, `is`(2))
        assertThat(origEdgeView.isAdjusted, `is`(false))
    }

    @Test
    fun shouldLayoutWestOfNodeView() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVertice(createVerticeView(150, 0, Direction.SOUTH))
        val origEdgeView = builder.connect(v1, v2)
        builder.split(origEdgeView, 0, Point2D(150, 100), v3)

        v1.moveBy(-10.0, 0.0)

        assertThat(origEdgeView.segmentPointCount, `is`(2))
    }

    @Test
    fun shouldLayoutEastOfNodeView() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val v4 = builder.addVertice(createVerticeView(150, 0, Direction.SOUTH))
        val origEdgeView = builder.connect(v1, v2)
        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v4)

        v2.moveBy(0.0, -10.0)

        assertThat(splitResult.tailEdgeView.segmentPointCount, `is`(4))
    }

    @Test
    fun shouldLayoutNorthOfNodeView() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVertice(createVerticeView(200, 0, Direction.WEST))
        val origEdgeView = builder.connect(v1, v2)
        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

        assertThat(splitResult.newEdgeView.segmentPointCount, `is`(3))
    }

    @Test
    fun shouldLayoutVerticalOpenVerticeView() {
        val v = builder.addVertice(createVerticeView(100, 100, Direction.SOUTH))
        val edgeView = builder.connectOpen(v, Point2D(100, 200))
        assertThat(edgeView.segmentPointCount, `is`(2))
    }

    @Test
    fun shouldLayoutVerticalSplitOpenVerticeView() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val ev = builder.connect(v1, v2)

        val splitResult = builder.split(ev, 0, Point2D(150, 100), null)
        splitResult.newEdgeView.moveDestinationEndPoint(200.0, 200.0)

        assertThat(splitResult.newEdgeView.segmentPointCount, `is`(3))
        assertThat(splitResult.newEdgeView.getSegmentDirection(0), `is`(Direction.SOUTH))
    }

    @Test
    fun shouldCooperateWithNonLayoutEdgeView() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val origEdgeView = builder.connect(v1, v2)
        origEdgeView.layout = Layout.NONE
        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

        assertThat(splitResult.newEdgeView.getSegmentDirection(0), `is`(Direction.SOUTH))
    }


    private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView {
        return TestVerticeView(loc = Point2D(x, y), inputDirection =  dir, portViewLength = 20)
    }
}