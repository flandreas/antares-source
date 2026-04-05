package io.antarescircuit.jabbah.graph.view.net

import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import kotlin.test.*

/**
 * If an [EdgeView] between two [NodeView]s is deleted, the [Net] must be divided.
 * Similar to [DivideNetTest], but with one more [NodeView].
 */
class DeleteSecondIntermediateEdgeViewTest : AbstractForkEdgeViewTest() {

    @Test
    fun shouldDeleteIntermediateEdgeViews() {
        GraphViewModule.graphViewAppService.delete(
            listOf(ev3),
            drawingViewBuilder.build<GraphElementView<GraphElement>>())

        assertViews()
        assertNets()

        assertNull(GraphViewConsistencyCheck.execute(builder.graphView))
    }

    private fun assertViews() {
        assertEquals(1, builder.graphView.getNodeViews().size)
        assertEquals(4, builder.graphView.getEdgeViews().size)
    }

    private fun assertNets() {
        assertEquals(2, builder.graph.elements.count { it is Net<*> })

        val net1 = v1.model.getOutput<Boolean>().net
        assertSame(net1, v2.model.getInput<Boolean>().net)
        assertSame(net1, v3.model.getInput<Boolean>().net)

        val net2 = v4.model.getInput<Boolean>().net
        assertSame(net2, v5.model.getInput<Boolean>().net)

        assertNotSame(net1, net2)
    }
}
