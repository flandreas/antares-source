package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.net.edge.Layout
import ch.scorpion.jabbah.graph.view.net.edge.OrthoEdgeViewLayout
import ch.scorpion.jabbah.graph.view.net.node.NodeViewImpl
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Integration tests for integrating [OrthoEdgeViewLayout] and [NodeViewImpl].
 */
class OrthoEdgeViewLayoutIntegrationTest {

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
        assertFalse(splitResult.tailEdgeView.isAdjusted)

        assertEquals(3, splitResult.newEdgeView.segmentPointCount)
        assertFalse(splitResult.newEdgeView.isAdjusted)

        assertEquals(2, origEdgeView.segmentPointCount)
        assertFalse(origEdgeView.isAdjusted)
    }

    @Test
    fun shouldSplitEdgeViewAtCorner() {
        val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVerticeView(createVerticeView(200, 200, Direction.WEST))
        val origEdgeView = builder.connect(v1, v3)

        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v2)

        assertEquals(3, splitResult.tailEdgeView.segmentPointCount)
        assertFalse(splitResult.tailEdgeView.isAdjusted)

        assertEquals(2, splitResult.newEdgeView.segmentPointCount)
        assertFalse(splitResult.newEdgeView.isAdjusted)

        assertEquals(2, origEdgeView.segmentPointCount)
        assertFalse(origEdgeView.isAdjusted)
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
        val edgeView = builder.connectOpen(v, Point2D(100, 200))
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
        origEdgeView.layout = Layout.NONE
        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

        assertEquals(Direction.SOUTH, splitResult.newEdgeView.getSegmentDirection(0))
    }


    private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView {
        return TestVerticeView(loc = Point2D(x, y), inputDirection =  dir, portViewLength = 20)
    }
}