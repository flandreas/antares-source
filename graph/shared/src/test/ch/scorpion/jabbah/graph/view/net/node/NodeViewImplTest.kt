package ch.scorpion.jabbah.graph.view.net.node

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.connect.SplitEdgeViewResult
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.graph.view.net.edge.OrthoEdgeViewLayout
import kotlin.test.*

/**
 * Unit tests for [NodeViewImpl].
 * Note that some of these unit tests heavily rely on [OrthoEdgeViewLayout] behaviour, which is supposed to be the
 * default layout strategy of newly created [EdgeView]s.
 */
class NodeViewImplTest {

    companion object {
	    init {
	    	GraphViewTestRule.configure()
	    }
    }

    private lateinit var builder: GraphViewBuilder<Boolean>
    private lateinit var v1: VerticeView<out Vertice>
    private lateinit var v2: VerticeView<out Vertice>
    private lateinit var v3: VerticeView<out Vertice>
    private lateinit var origEdgeView: EdgeView<Boolean>
    private lateinit var splitResult: SplitEdgeViewResult<Boolean>

    @BeforeTest
    fun setup(){
        TestTranslationsBuilder().withAnyKey()
        builder = GraphViewBuilder()
        v1 = builder.addVerticeView(TestVerticeView(loc = Point2D(100, 100)))
        v2 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 100)))
        v3 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 200)))
        origEdgeView = builder.connect(v1, v2)
        splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)
    }

    @Test
    fun testSetup() {
        assertEquals(2, splitResult.tailEdgeView.segmentPointCount)
        assertFalse(splitResult.tailEdgeView.isAdjusted)
        assertEquals(3, splitResult.newEdgeView.segmentPointCount)
        assertFalse(splitResult.newEdgeView.isAdjusted)
        assertEquals(2, origEdgeView.segmentPointCount)
        assertFalse(origEdgeView.isAdjusted)
    }

    @Test
    fun shouldYieldEdgeViews() {
        val list = splitResult.nodeView.getEdgeViews()
        assertEquals(3, list.size)
        assertTrue(list.contains(origEdgeView))
        assertTrue(list.contains(splitResult.newEdgeView))
        assertTrue(list.contains(splitResult.tailEdgeView))
    }

    @Test
    fun shouldYieldIncomingEdgeViews() {
        assertSame(origEdgeView, splitResult.nodeView.getIncomingEdgeView())
    }

    @Test
    fun shouldYieldOutgoingEdgeViews() {
        val list = splitResult.nodeView.getOutgoingEdgeViews()
        assertEquals(2, list.size)
        assertTrue(list.contains(splitResult.newEdgeView))
        assertTrue(list.contains(splitResult.tailEdgeView))
    }

    @Test
    fun shouldYieldEdgeViewForDirection() {
        assertSame(origEdgeView, splitResult.nodeView.getEdgeView(Direction.WEST))
        assertSame(splitResult.tailEdgeView, splitResult.nodeView.getEdgeView(Direction.EAST))
        assertSame(splitResult.newEdgeView, splitResult.nodeView.getEdgeView(Direction.SOUTH))
    }

    @Test
    fun shouldCalculatePortConnectionLayoutDirections() {
        val set = splitResult.nodeView.getPortConnectionLayoutDirections(splitResult.newEdgeView, null, Point2D(200, 200))
        assertTrue(set.contains(Direction.SOUTH))
        assertFalse(set.contains(Direction.NORTH))
	    assertFalse(set.contains(Direction.EAST))
	    assertFalse(set.contains(Direction.WEST))
    }
}