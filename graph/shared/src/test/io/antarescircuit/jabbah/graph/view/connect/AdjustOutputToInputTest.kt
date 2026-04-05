package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdjustOutputToInputTest : AbstractInputEventHandlerTest() {

    init {
        handler = GraphViewModule.outputToInputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    @Test
    fun shouldPreferInputPortViewDirection() {
        mouseMoveToAndClickAt(130, 100)
        mouseMoveToAndClickAt(150, 200)

        mouseMoveTo(190, 100)

        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(Direction.EAST, ev.getSegmentDirection(0))
    }

    @Test
    fun shouldLayoutStraightOnSnapToInput() {
        mouseMoveToAndClickAt(130, 100)
        mouseMoveTo(190, 100)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(2, ev.segmentPointCount)
    }

    @Test
    fun shouldLayoutStraightOnConnectToInput() {
        mouseMoveToAndClickAt(130, 100)
        mouseMoveToAndClickAt(190, 100)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(2, ev.segmentPointCount)
    }
}