package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.AbstractInputEventHandlerTest
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import dev.mokkery.verify
import kotlin.test.*

class EdgeToPortAdjustConnectorTest : AbstractInputEventHandlerTest() {

    @BeforeTest
    fun initialize() {
        handler = GraphViewModule.edgeToPortOrEdgeConnector.handler
        GraphViewModule.graphViewConnectService.addConnection<Boolean>(builder.graphView, v1, v2)
        builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        editor.commandManager.reset()
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    @Test
    fun shouldAdjustToPortView() {
        connectAdjusted()

        assertEdgeToPortConnected(builder.graphView.getVerticeView("v3")!!.model.getInput())
        assertAdjustedConnectionGeometry()
    }

    @Test
    fun shouldUndoAdjustToPortView() {
        connectAdjusted()

        editor.commandManager.undo()

        assertUnconnected()
    }

    @Test
    fun shouldRedoAdjustToPortView() {
        connectAdjusted()
        editor.commandManager.undo()

        editor.commandManager.redo()

        assertEdgeToPortConnected(builder.graphView.getVerticeView("v3")!!.model.getInput())
        assertAdjustedConnectionGeometry()
    }

    @Test
    fun shouldCancelAdjustWithEscapePressed() {
        mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
        clickMouseAt(150, 100)
        clickMouseAt(160, 300)
        clickMouseAt(170, 300)

        pressEscape()
        assertEquals(4, draggedEdgeView.segmentPointCount)

        pressEscape()
        assertEquals(3, draggedEdgeView.segmentPointCount)

        pressEscape()
        assertEquals(1, builder.graphView.getEdgeViews().size)
        assertEquals(0, builder.graphView.getNodeViews().size)
    }

    @Test
    fun shouldStartLayoutPerpendicularToEdgeView() {
        mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
        clickMouseAt(150, 100)
        // Intentionally move along the starting EdgeView for a bit
        mouseMoveTo(130, 100)
        // Now move vertically below the starting point
        mouseMoveTo(150, 200)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(2, ev.segmentPointCount)
    }

    @Test
    fun shouldPreferSimplestSolutionAtCorner() {
        // Create an EdgeView with a corner
        val v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 200))
        builder.connectOutputOpen(v4, Point2D(150, 300))

        // Connect the corner to v3
        mouseMoveTo(150, 200, modifiers = Modifier.Alt.mask)
        clickMouseAt(150, 200, modifiers = Modifier.Alt.mask)
        mouseMoveTo(190, 200)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(2, ev.segmentPointCount)
    }

    @Test
    fun shouldPreferSimplestSolutionInsideSegment() {
        // Create an EdgeView with a second, vertical segment
        val v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 150))
        builder.connectOutputOpen(v4, Point2D(150, 300))

        // Connect the segment to v3
        mouseMoveTo(150, 200, modifiers = Modifier.Alt.mask)
        clickMouseAt(150, 200, modifiers = Modifier.Alt.mask)
        mouseMoveTo(190, 200)

        val ev = builder.graphView.getEdgeViews().first()
        assertEquals(2, ev.segmentPointCount)
    }

    private fun connectAdjusted() {
        mouseMoveTo(150, 100, modifiers = Modifier.Alt.mask)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        verify { view.setCursor(Cursor.CROSSHAIR) }

        clickMouseAt(150, 100)
        assertFalse(ConnectionPointHighlighter.hasPortViewHighlight)

        clickMouseAt(160, 300)
        clickMouseAt(170, 300)
        clickMouseAt(180, 200)

        mouseMoveTo(190, 200)
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)

        clickMouseAt(190, 200)
    }

    private fun assertAdjustedConnectionGeometry() {
        val newEv = builder.graphView.getEdgeViews().first()

        assertEquals(
            listOf(
                Point2D(150, 100),
                Point2D(150, 300),
                Point2D(180, 300),
                Point2D(180, 200),
                Point2D(200, 200)),
            newEv.polyline.getPoints(0, newEv.segmentPointCount)
        )
    }

    // 3 VerticeViews, 1 NodeView, 3 EdgeViews
    private fun assertEdgeToPortConnected(destPort: Port<out Boolean>, drawablesCount: Int = 7) {
        // Instances have been recreated while replaying from undo snapshot
        @Suppress("UNCHECKED_CAST")
        val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
        val v1 = builder.graphView.getVerticeView("v1")!!
        val v2 = builder.graphView.getVerticeView("v2")!!

        assertTrue(nodeView.model.isConnectedWith(v1.model.getOutput()))
        assertTrue(nodeView.model.isConnectedWith(v2.model.getInput()))
        assertTrue(nodeView.model.isConnectedWith(destPort))

        assertEquals(drawablesCount, builder.graphView.drawables.size)
    }

    private fun assertUnconnected() {
        // 3 VerticeViews, 0 NodeViews, 1 EdgeView
        val v1 = builder.graphView.getVerticeView("v1")!!
        val v2 = builder.graphView.getVerticeView("v2")!!
        val net = builder.graphView.graph!!.elements.filterIsInstance<Net<*>>().first()

        assertTrue(net.isConnectedWith(v1.model.getOutput()))
        assertTrue(net.isConnectedWith(v2.model.getInput()))
        assertEquals(4, builder.graphView.drawables.size)
    }
}