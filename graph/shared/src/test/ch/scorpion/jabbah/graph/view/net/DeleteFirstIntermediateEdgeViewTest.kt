package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.graph.health.GraphViewConsistencyCheck
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import kotlin.test.*

class DeleteFirstIntermediateEdgeViewTest : AbstractForkEdgeViewTest() {

    @Test
    fun shouldDeleteFirstIntermediateEdgeView() {
        GraphViewModule.graphViewAppService.delete(
            listOf(ev2),
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

        val net2 = v3.model.getInput<Boolean>().net
        assertSame(net2, v4.model.getInput<Boolean>().net)
        assertSame(net2, v5.model.getInput<Boolean>().net)

        assertNotSame(net1, net2)
    }
}