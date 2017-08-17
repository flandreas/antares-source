package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Integration tests for moving [EdgeViewImpl] segments with and without [NodeView]s.
 */
class MoveSegmentIntegrationTest {

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
    fun shouldMoveSegmentUp() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val ev = builder.connect(v1, v2)

        val info = ev.moveSegment(0, -20.0)

        assertThat(ev.segmentPointCount, `is`(6))
        assertThat(info.segmentIndex, `is`(2))
    }

    @Test
    fun shouldMoveSegmentUpAndBack() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val ev = builder.connect(v1, v2)

        ev.moveSegment(0, -20.0)
        val info = ev.moveSegment(2, 20.0)

        assertThat(ev.segmentPointCount, `is`(2))
        assertThat(info.segmentIndex, `is`(0))
    }

    @Test
    fun shouldMoveSegmentUpWithAdjacentNode() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 100, Direction.WEST))
        val v3 = builder.addVertice(createVerticeView(200, 0, Direction.WEST))
        val origEdgeView = builder.connect(v1, v2)
        val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

        origEdgeView.moveSegment(0, -50.0)

        assertThat(origEdgeView.segmentPointCount, `is`(4))
        assertThat(splitResult.nodeView.location, `is`(Point2D(150, 50)))
        assertThat(splitResult.tailEdgeView.segmentPointCount, `is`(3))
        assertThat(splitResult.newEdgeView.segmentPointCount, `is`(3))
    }

    @Test
    fun shouldMoveSegmentAtRotatedVertice() {
        val v1 = builder.addVertice(createVerticeView(100, 100, Direction.EAST))
        val v2 = builder.addVertice(createVerticeView(200, 0, Direction.WEST))
        v2.rotation = Rotation.R90
        val ev = builder.connect(v1, v2)

        ev.moveSegment(1, 20.0)
        assertThat(ev.segmentPointCount, `is`(5))
        for (i in 0..ev.segmentPointCount - 2) {
            assertThat("Segment $i is not orthogonal", ev.isSegmentOrthogonal(i), `is`(true))
        }
    }

    private fun createVerticeView(x: Int, y: Int, dir: Direction): TestVerticeView {
        return TestVerticeView(DrawStyleModule.styleProvider, TestVertice(), Point2D(x, y), dir, 20)
    }
}