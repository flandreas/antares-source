package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputToNodeConnectorTest : AbstractInputEventHandlerTest() {

    private val v3: TestVerticeView
    private val v4: TestVerticeView

    init {
        handler = GraphViewModule.inputToOutputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout

        v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 0))
        v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 200, 200))

        val edgeView = builder.connect(v1, v2)
        builder.split(edgeView, 0, Point2D(150, 100), v3.getPortView(v3.model.getInput()))
    }

    /** Regression test for bug #1229. */
    @Test
    fun shouldCreateNewNodeOnSnappedEdgeView() {
        mouseMoveTo(190, 200)
        pressMouseAt(190, 200)

        // Snap to EdgeView to the right of NodeView
        dragMouseTo(165, 100)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

        // Release the mouse (along the EdgeView!) slightly above the NodeView
        dragMouseTo(150, 100)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        releaseMouseAt(150, 100)

        assertEquals(2, builder.graphView.getNodeViews().size)
        assertEquals(5, builder.graphView.getEdgeViews().size)

        builder.graphView.getEdgeViews().forEach { ev ->
            for (i in 0 until ev.segmentPointCount - 1) {
                assertTrue(ev.polyline.isSegmentOrthogonal(i))
            }
        }
    }
}