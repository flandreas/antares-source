package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [GraphViewImpl].
 */
class GraphViewImplTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    @Before
    fun setup() {
	    TestTranslationsBuilder().withAnyKey()
    }

    private val graphView: GraphView<GraphElementView<*>> = GraphViewImpl(
            GraphImpl(),
            IOModule.storableClonerProvider.invoke(),
            GraphViewModule.outputToInputConnector,
            GraphViewModule.inputToOutputOrEdgeConnector,
            GraphViewModule.reconnectOriginConnector,
            GraphViewModule.reconnectDestinationConnector,
            BaseModule.eventBus)

    @Test
    fun shouldAddToModel() {
        val verticeView = TestVerticeView()
        graphView.add(verticeView)

        assertThat(graphView.contains(verticeView), `is`(true))
        assertThat(graphView.graph!!.contains(verticeView.vertice), `is`(true))
    }

    @Test
    fun shouldRemoveFromModel() {
        val verticeView = TestVerticeView()
        graphView.add(verticeView)
        graphView.remove(verticeView)

        assertThat(graphView.contains(verticeView), `is`(false))
        assertThat(graphView.graph!!.contains(verticeView.vertice), `is`(false))
    }

    @Test
    fun shouldClearModel() {
        val verticeView = TestVerticeView()
        graphView.add(verticeView)
        graphView.clear()

        assertThat(graphView.contains(verticeView), `is`(false))
        assertThat(graphView.graph!!.contains(verticeView.vertice), `is`(false))
    }
}