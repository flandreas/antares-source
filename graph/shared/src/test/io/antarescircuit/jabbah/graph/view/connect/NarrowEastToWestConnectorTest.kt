package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.geom.Direction.EAST
import io.antarescircuit.jabbah.base.geom.Direction.WEST
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Regression test for bug #1265.
 */
class NarrowEastToWestConnectorTest : AbstractInputEventHandlerTest() {

    companion object {
        private const val PORT_VIEW_LENGTH = 14 // value from antares module where bug occurred
        private const val DX = 62
    }

    private val v3: TestVerticeView
    private val v4: TestVerticeView

    init {
        v3 = builder.addVerticeView(TestVerticeView("v3", loc = Point2D(100, 200), inputDirection = WEST,
            outputDirection = EAST, width = TestVerticeView.DEF_SIZE, portViewLength = PORT_VIEW_LENGTH))
        v4 = builder.addVerticeView(TestVerticeView("v4", loc = Point2D(100 + DX, 300), inputDirection = WEST,
            outputDirection = EAST, width = TestVerticeView.DEF_SIZE, portViewLength = PORT_VIEW_LENGTH))
    }

    @Test
    fun shouldAdjustWestToEast() {
        handler = GraphViewModule.outputToInputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints

        mouseMoveToAndClickAt(130, 200)
        mouseMoveTo(100 + DX - 10, 300)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

        clickMouseAt(100 + DX - 10, 300)

        assertSame(v3.model.getOutput<Any>().net, v4.model.getInput<Any>().net)
        assertEquals(4, (editor.drawing as GraphView).getEdgeViews().first().polyline.pointsCount)
    }

    @Test
    fun shouldAdjustEastToWest() {
        handler = GraphViewModule.inputToOutputOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints

        mouseMoveToAndClickAt(100 + DX - 10, 300)
        mouseMoveTo(130, 200)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

        clickMouseAt(130, 200)

        assertSame(v3.model.getOutput<Any>().net, v4.model.getInput<Any>().net)
        assertEquals(4, (editor.drawing as GraphView).getEdgeViews().first().polyline.pointsCount)
    }
}