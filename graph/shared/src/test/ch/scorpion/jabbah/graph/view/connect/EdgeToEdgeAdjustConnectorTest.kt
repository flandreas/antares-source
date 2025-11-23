package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier.Alt
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EdgeToEdgeAdjustConnectorTest : AbstractInputEventHandlerTest(GraphViewModule.edgeToPortOrEdgeConnector.handler) {

    companion object {
        init {
            GraphViewTestRule.configure()
        }
    }

    private val v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
    private val v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 300))
    private val v5 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v5", 200, 300))

    @BeforeTest
    fun initialize() {
        val ev1 = builder.connect(v1, v2)
        builder.split(ev1, 0, Point2D(150, 100), v3)
        builder.connect(v4, v5)
        EditModule.drawingAppService.delete(listOf(ev1), editor.view)

        editor.commandManager.reset()
        CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
    }

    @Test
    fun shouldAdjustEdgeViewToEdgeView() {
        adjust()
        assertConnected()
    }

    private fun adjust() {
        mouseMoveTo(150, 300, modifiers = Alt.mask)
        clickMouseAt(150, 300)
        clickMouseAt(100, 250)

        mouseMoveTo(150, 200)
        clickMouseAt(150, 200)
    }

    private fun assertConnected() {
        val nodeView = builder.graphView.getDrawable { it is NodeView<*> } as NodeView<Boolean>
        val v2 = builder.graphView.getVerticeView("v2")!!
        val v3 = builder.graphView.getVerticeView("v3")!!
        val v4 = builder.graphView.getVerticeView("v4")!!
        val v5 = builder.graphView.getVerticeView("v5")!!

        assertEquals(1, builder.graphView.graph!!.elements.filterIsInstance<Net<*>>().size)
        assertTrue(nodeView.model.isConnectedWith(v2.model.getInput()))
        assertTrue(nodeView.model.isConnectedWith(v3.model.getInput()))
        assertTrue(nodeView.model.isConnectedWith(v4.model.getOutput()))
        assertTrue(nodeView.model.isConnectedWith(v5.model.getInput()))
    }
}