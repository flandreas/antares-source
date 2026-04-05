package io.antarescircuit.jabbah.graph.view.net

import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeleteForkToothEdgeViewTest : AbstractForkEdgeViewTest() {

    private fun getToothEdgeView(name: String): EdgeView<*> {
        return builder.graphView.getEdgeViews().first {
            it.destination?.connectableView is VerticeView<*>
                && (it.destination?.connectableView as VerticeView<*>).model.name == name
        }
    }

    @Test
    fun shouldDeleteFirstToothEdgeViews() {
        assertDeleteToothEdgeView("v2")
    }

    @Test
    fun shouldDeleteSecondToothEdgeView() {
        assertDeleteToothEdgeView("v3")
    }

    private fun assertDeleteToothEdgeView(name: String) {
        GraphViewModule.graphViewAppService.delete(
            listOf(getToothEdgeView(name)),
            drawingViewBuilder.build<GraphElementView<GraphElement>>())

        assertNets()
        assertNull(GraphViewConsistencyCheck.execute(builder.graphView))
    }

    private fun assertNets() {
        assertEquals(1, builder.graph.elements.count { it is Net<*> })
    }
}