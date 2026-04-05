package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdjustInputToOutputTest : AbstractInputEventHandlerTest() {

    init {
        handler = GraphViewModule.inputToOutputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    @Test
    fun shouldPreferOutputPortViewDirection() {
        mouseMoveToAndClickAt(190, 100)
        mouseMoveToAndClickAt(150, 200)

        mouseMoveTo(130, 100)

        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(Direction.EAST, ev.getSegmentDirection(0))
    }

    @Test
    fun shouldLayoutStraightOnSnapToOutput() {
        mouseMoveToAndClickAt(190, 100)
        mouseMoveTo(130, 100)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(2, ev.segmentPointCount)
    }

    @Test
    fun shouldLayoutStraightOnConnectToOutput() {
        mouseMoveToAndClickAt(190, 100)
        mouseMoveToAndClickAt(130, 100)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(2, ev.segmentPointCount)
    }
}