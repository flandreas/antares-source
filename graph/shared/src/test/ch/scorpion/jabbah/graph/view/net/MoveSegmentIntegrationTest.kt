package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for moving [EdgeViewImpl] segments with and without [NodeView]s.
 */
class MoveSegmentIntegrationTest {

    companion object {
	    init {
		    GraphViewTestRule.configure()
	    }
    }

    private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()

    @Test
    fun shouldMoveSegmentUp() {
        val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
        val ev = builder.connect(v1, v2)

        val info = ev.moveSegment(0, -20.0)

        assertEquals(6, ev.segmentPointCount)
        assertEquals(2, info.segmentIndex)
    }

    @Test
    fun shouldMoveSegmentUpAndBack() {
        val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
        val ev = builder.connect(v1, v2)

        ev.moveSegment(0, -20.0)
        val info = ev.moveSegment(2, 20.0)

        assertEquals(2, ev.segmentPointCount)
        assertEquals(0, info.segmentIndex)
    }

    @Test
    fun shouldMoveSegmentUpWithAdjacentNode() {
        val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVerticeView(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVerticeView(createVerticeView(200, 0, Direction.WEST))
        val origEdgeView = builder.connect(v1, v2)
        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

        origEdgeView.moveSegment(0, -50.0)

        assertEquals(4, origEdgeView.segmentPointCount)
        assertEquals(Point2D(150, 50), splitResult.nodeView.location)
        assertEquals(3, splitResult.tailEdgeView.segmentPointCount)
        assertEquals(3, splitResult.newEdgeView.segmentPointCount)
    }

    @Test
    fun shouldMoveSegmentAtRotatedVertice() {
        val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVerticeView(createVerticeView(200, 0, Direction.WEST))
        v2.rotation = Rotation.R90
        val ev = builder.connect(v1, v2)

        ev.moveSegment(1, 20.0)
        assertEquals(5, ev.segmentPointCount)
        for (i in 0..ev.segmentPointCount - 2) {
            assertTrue(ev.isSegmentOrthogonal(i), "Segment $i is not orthogonal")
        }
    }

	@Test
	fun shouldNotCurlOrigin() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 200, Direction.WEST))
		val ev = builder.connect(v1, v2)

		ev.moveSegment(0, -20.0)
		v1.moveBy(0.0, -20.0)

		assertEquals(4, ev.segmentPointCount)
	}

	@Test
	fun shouldNotCurlDestination() {
		val v1 = builder.addVerticeView(createVerticeView(100, 100, Direction.EAST))
		val v2 = builder.addVerticeView(createVerticeView(200, 200, Direction.WEST))
		val ev = builder.connect(v1, v2)

		ev.moveSegment(2, 20.0)
		v2.moveBy(0.0, 20.0)

		assertEquals(4, ev.segmentPointCount)
	}

    private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView {
        return TestVerticeView(loc = Point2D(x, y), inputDirection = dir, portViewLength = 20)
    }
}