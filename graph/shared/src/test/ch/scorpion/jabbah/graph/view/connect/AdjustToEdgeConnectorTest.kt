package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdjustToEdgeConnectorTest : AbstractInputEventHandlerTest() {

    init {
        handler = GraphViewModule.inputToOutputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    /** Regression test for GitHub #1140. */
    @Test
    fun shouldNotMovePointWhenSnappingToEdgeView() {
        // Create a z-shaped connection with the vertical segment at x=160
        val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        builder.connect(v1, v3)

        // Start connection at v2
        mouseMoveTo(190, 100)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        clickMouseAt(190, 100)

        // Create intermediate point
        mouseMoveTo(170, 100)
        clickMouseAt(170, 100)

        // Move onto z-shaped connection to snap to it
        mouseMoveTo(160, 100)

        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        val ev2 = builder.graphView.getEdgeViews().first()
        assertEquals(3, ev2.segmentPointCount)
    }

    @Ignore
    @Test
    fun shouldMeetEdgeViewOrthogonally() {
        // Create a z-shaped connection with the vertical segment at x=160
        val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        builder.connect(v1, v3)
        val v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 200, 150))

        // Start connection at v4
        mouseMoveTo(190, 150)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        clickMouseAt(190, 150)

        // Create intermediate point
        mouseMoveTo(170, 150)
        clickMouseAt(170, 150)

        // Move a little bit more to the left
        mouseMoveTo(165, 150) // This produced the bug!

        // Move onto z-shaped connection to snap on it, but not on the same y value
        mouseMoveTo(160, 140)

        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        val ev2 = builder.graphView.getEdgeViews().first()
        assertEquals(4, ev2.segmentPointCount)
        assertEquals(Direction.EAST, ev2.getSegmentDirection(0))
        assertEquals(Point2D(165, 140), ev2.getSegmentPoint(1))
    }
}