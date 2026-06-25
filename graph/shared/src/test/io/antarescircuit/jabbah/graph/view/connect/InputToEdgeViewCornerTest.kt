package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Point2D
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
 * Regression test for bug #1214.
 */
class InputToEdgeViewCornerTest : AbstractInputEventHandlerTest() {

    private lateinit var v4: VerticeView<*>

    init {
        handler = GraphViewModule.inputToOutputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout

        val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 300))
        val ev = builder.connectOutputOpen(v4, Point2D(400, 300))
        builder.split(ev, 0, Point2D(150, 300), v3)
    }

    @Test
    fun shouldConnectToEdgeViewCorner() {
        mouseMoveTo(190, 100)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        pressMouseAt(190, 100)

        // Target horizontal segment by dragging slightly to the right of x=150
        dragMouseTo(155, 200)
        assertEquals(3, (editor.drawing as GraphView).getEdgeViews().first().polyline.pointsCount)
        releaseMouseAt(155, 200)

        val evBetweenNodes = (editor.drawing as GraphView).getEdgeViews().first { it.id == 8 }

        // Before the bugfix, the vertical segment of the target EdgeView had 5 points,
        // resulting from going to the west instead of right down to south.
        assertEquals(2, evBetweenNodes.polyline.pointsCount)
    }

    @Test
    fun shouldPreferLeastComplexOutgoingEdgeViewWhileDragging() {
        mouseMoveTo(190, 100)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        pressMouseAt(190, 100)

        dragMouseTo(150, 200)
        assertEquals(3, (editor.drawing as GraphView).getEdgeViews().first().polyline.pointsCount)
    }

    @Test
    fun shouldPreferLeastComplexIncomingEdgeViewWhileDragging() {
        handler = GraphViewModule.outputToInputOrEdgeConnector.handler

        // Delete v4 so that target Net has only InputPorts and can accept a new OutputPort
        GraphViewModule.graphViewAppService.delete(listOf(v4), editor.view)

        // Start EdgeView at v1
        mouseMoveTo(130, 100)
        pressMouseAt(130, 100)
        dragMouseTo(150, 200)

        assertEquals(3, (editor.drawing as GraphView).getEdgeViews().first().polyline.pointsCount)
    }
}