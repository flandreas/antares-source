package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsService
import io.antarescircuit.jabbah.graph.view.connect.unconnected.FindUnconnectedPortsType
import io.antarescircuit.jabbah.graph.view.connect.unconnected.UnconnectedPort
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FindUnconnectedPortsServiceTest {

    private val builder: GraphViewBuilder<Boolean>

    init {
        GraphViewTestRule.configure()
        builder = GraphViewBuilder<Boolean>("Test")
    }

    @Test
    fun shouldFindUnconnectedInputsInMetaGraph() {
        val metaGraph = createMetaGraph()

        val result = FindUnconnectedPortsService.findInMetaGraph(metaGraph, FindUnconnectedPortsType.Inputs)

        assertEquals(1, result.size)
        assert(result.first(), metaGraph.uuid, 1, 1)
    }

    @Test
    fun shouldFindUnconnectedOutputsInMetaGraph() {
        val metaGraph = createMetaGraph()

        val result = FindUnconnectedPortsService.findInMetaGraph(metaGraph, FindUnconnectedPortsType.Outputs)

        assertEquals(1, result.size)
        assert(result.first(), metaGraph.uuid, 1, 2)
    }

    @Test
    fun shouldFindUnconnectedPortsInMetaGraph() {
        val metaGraph = createMetaGraph()

        val result = FindUnconnectedPortsService.findInMetaGraph(metaGraph, FindUnconnectedPortsType.All)

        assertEquals(1, result.size)
        assert(result.first(), metaGraph.uuid, 1, 1, 2)
    }

    private fun createMetaGraph(): MetaGraph {
        val verticeView = TestVerticeView()
        builder.addVerticeView(verticeView)
        return MetaGraph(builder.graphStorable, ContainerDrawing())
    }

    private fun assert(result: UnconnectedPort, uuid: UUID, verticeViewId: Int, vararg portIds: Int) {
        assertEquals(uuid, result.metaGraphId)
        assertEquals(verticeViewId, result.verticeViewId)
        assertEquals(portIds.size, result.portIds.size)
        assertTrue(portIds.all { result.portIds.contains(it) })
    }
}