package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier.Alt
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlightCircle
import ch.scorpion.jabbah.graph.view.connect.highlight.ConnectionPointHighlighter
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EdgeToEdgeAdjustConnectorTest : AbstractInputEventHandlerTest() {

    @BeforeTest
    fun initialize() {
        handler = GraphViewModule.edgeToPortOrEdgeConnector.handler
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    @Test
    fun shouldConnectInOutNetToMultiInputNet() {
        // Setup
        val v3: VerticeView<*> = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        val v4: VerticeView<*> = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 300))
        val v5: VerticeView<*> = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v5", 200, 300))
        val ev1 = builder.connect(v1, v2)
        builder.split(ev1, 0, Point2D(150, 100), v3)
        builder.connect(v4, v5)
        EditModule.drawingAppService.delete(listOf(ev1), editor.view)
        editor.commandManager.reset()

        // Connect
        mouseMoveTo(150, 300, modifiers = Alt.mask)
        clickMouseAt(150, 300)
        clickMouseAt(100, 250)
        mouseMoveTo(150, 200)
        clickMouseAt(150, 200)

        // Assert
        val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
        assertEquals(1, builder.graphView.graph!!.elements.filterIsInstance<Net<*>>().size)
        assertTrue(nodeView.model.isConnectedWith(builder.graphView.getVerticeView("v2")!!.model.getInput()))
        assertTrue(nodeView.model.isConnectedWith(builder.graphView.getVerticeView("v3")!!.model.getInput()))
        assertTrue(nodeView.model.isConnectedWith(builder.graphView.getVerticeView("v4")!!.model.getOutput()))
        assertTrue(nodeView.model.isConnectedWith(builder.graphView.getVerticeView("v5")!!.model.getInput()))
    }

    /** This is the same as [shouldConnectInOutNetToMultiInputNet], but in the opposite direction. */
    @Test
    fun shouldConnectMultiInputNetToInOutNet() {
        // Setup
        val v3: VerticeView<*> = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
        val v4: VerticeView<*> = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 300))
        val v5: VerticeView<*> = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v5", 200, 300))
        val ev1 = builder.connect(v1, v2)
        builder.split(ev1, 0, Point2D(150, 100), v3)
        builder.connect(v4, v5)
        EditModule.drawingAppService.delete(listOf(ev1), editor.view)
        editor.commandManager.reset()

        // Connect
        mouseMoveToAndClickAt(150, 200, modifiers = Alt.mask)
        mouseMoveTo(150, 300)

        // Assert acceptance
        assertTrue(ConnectionPointHighlighter.hasPortViewHighlight)
        assertIs<ConnectionPointHighlightCircle>(ConnectionPointHighlighter.portViewHighlight)
    }
}