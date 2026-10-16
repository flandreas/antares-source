package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression test for bug #1265.
 */
class InputToEdgeViewOptimalSegmentTest : AbstractInputEventHandlerTest() {

    private val v4: VerticeView<*>

    init {
        handler = GraphViewModule.inputToOutputOrEdgeConnector.handler

        val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        v4 = builder.addVerticeView(TestVerticeView.createNorthInputVerticeView("v4", 160, 300))

        builder.connect(v1, v3)
    }

    @Test
    fun shouldSetPointsToCorner() {
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints

        mouseMoveTo(160, 290)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        clickMouseAt(160, 290)

        assertTrue(v4.model.getInput<Any>().isConnected)

        mouseMoveTo(160, 200)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        assertEquals(2, (editor.drawing as GraphView).getEdgeViews().first().polyline.pointsCount)
    }

    @Test
    fun shouldAutoLayoutToCorner() {
        CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout

        mouseMoveTo(160, 290)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        pressMouseAt(160, 290)

        assertTrue(v4.model.getInput<Any>().isConnected)

        dragMouseTo(160, 200)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        assertEquals(2, (editor.drawing as GraphView).getEdgeViews().first().polyline.pointsCount)
    }
}