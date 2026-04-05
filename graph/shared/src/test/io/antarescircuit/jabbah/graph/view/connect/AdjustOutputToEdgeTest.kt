package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdjustOutputToEdgeTest : AbstractInputEventHandlerTest() {

    init {
        handler = GraphViewModule.outputToInputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    @Test
    fun shouldAllowToContinueInSameDirectionToEdge() {
        // Create a target EdgeView with a vertical segment at x=170
        builder.connectInputOpen(v2, Point2D(170, 300))

        v1.moveBy(0.0, 50.0)
        mouseMoveToAndClickAt(130, 150)
        mouseMoveToAndClickAt(150, 140)

        // This should create an L-shape (continuing North), not a Z-shape
        mouseMoveTo(170, 130)

        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        val ev2 = builder.graphView.getEdgeViews().first()
        assertEquals(5, ev2.segmentPointCount)
    }
}