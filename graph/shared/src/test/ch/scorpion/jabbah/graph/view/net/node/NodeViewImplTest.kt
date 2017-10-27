package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.connect.SplitEdgeViewResult
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.graph.view.net.edge.OrthoEdgeViewLayout
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [NodeViewImpl].
 * Note that some of these unit tests heavily rely on [OrthoEdgeViewLayout] behaviour, which is supposed to be the
 * default layout strategy of newly created [EdgeView]s.
 */
class NodeViewImplTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    private lateinit var builder: GraphViewBuilder<Boolean>
    private lateinit var v1: VerticeView<out Vertice>
    private lateinit var v2: VerticeView<out Vertice>
    private lateinit var v3: VerticeView<out Vertice>
    private lateinit var origEdgeView: EdgeView<Boolean>
    private lateinit var splitResult: SplitEdgeViewResult<Boolean>

    @Before
    fun setup(){
        TestTranslationsBuilder().withAnyKey()
        builder = GraphViewBuilder()
        v1 = builder.addVertice(TestVerticeView(loc = Point2D(100, 100)))
        v2 = builder.addVertice(TestVerticeView(loc = Point2D(200, 100)))
        v3 = builder.addVertice(TestVerticeView(loc = Point2D(200, 200)))
        origEdgeView = builder.connect(v1, v2)
        splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)
    }

    @Test
    fun testSetup() {
        assertThat(splitResult.tailEdgeView.segmentPointCount, `is`(2))
        assertThat(splitResult.tailEdgeView.isAdjusted, `is`(false))
        assertThat(splitResult.newEdgeView.segmentPointCount, `is`(3))
        assertThat(splitResult.newEdgeView.isAdjusted, `is`(false))
        assertThat(origEdgeView.segmentPointCount, `is`(2))
        assertThat(origEdgeView.isAdjusted, `is`(false))
    }

    @Test
    fun shouldYieldEdgeViews() {
        val list = splitResult.nodeView.getEdgeViews()
        assertThat(list.size, `is`(3))
        assertThat(list.contains(origEdgeView), `is`(true))
        assertThat(list.contains(splitResult.newEdgeView), `is`(true))
        assertThat(list.contains(splitResult.tailEdgeView), `is`(true))
    }

    @Test
    fun shouldYieldIncomingEdgeViews() {
        assertThat(splitResult.nodeView.getIncomingEdgeView(), `is`(sameInstance(origEdgeView)))
    }

    @Test
    fun shouldYieldOutgoingEdgeViews() {
        val list = splitResult.nodeView.getOutgoingEdgeViews()
        assertThat(list.size, `is`(2))
        assertThat(list.contains(splitResult.newEdgeView), `is`(true))
        assertThat(list.contains(splitResult.tailEdgeView), `is`(true))
    }

    @Test
    fun shouldYieldEdgeViewForDirection() {
        assertThat(splitResult.nodeView.getEdgeView(Direction.WEST), `is`(sameInstance(origEdgeView)))
        assertThat(splitResult.nodeView.getEdgeView(Direction.EAST), `is`(sameInstance(splitResult.tailEdgeView)))
        assertThat(splitResult.nodeView.getEdgeView(Direction.SOUTH), `is`(sameInstance(splitResult.newEdgeView)))
    }

    @Test
    fun shouldCalculatePortConnectionLayoutDirections() {
        val set = splitResult.nodeView.getPortConnectionLayoutDirections(splitResult.newEdgeView, null, Point2D(200, 200))
        assertThat(set.contains(Direction.SOUTH), `is`(true))
        assertThat(set.contains(Direction.NORTH), `is`(false))
        assertThat(set.contains(Direction.EAST), `is`(false))
        assertThat(set.contains(Direction.WEST), `is`(false))
    }
}