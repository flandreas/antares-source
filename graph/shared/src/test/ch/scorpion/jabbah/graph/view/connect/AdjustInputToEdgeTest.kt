package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdjustInputToEdgeTest : AbstractInputEventHandlerTest() {

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

        // Move onto z-shaped connection to snap to it. Target the SECOND segment with y=105.
        mouseMoveTo(160, 105)

        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        val ev2 = builder.graphView.getEdgeViews().first()
        assertEquals(3, ev2.segmentPointCount)
    }
}