package ch.scorpion.jabbah.graph.model.graph

import com.nhaarman.mockito_kotlin.mock
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.model.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.ClassRule
import org.junit.Test
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.io.IOModule

/**
 * Unit tests for [GraphImpl].
 */
class GraphImplTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphModelTestRule()
    }

    @Test
    fun shouldClear() {
        val graph = GraphImpl(mock<EventBus>())
                .add(TestVertice())
                .add(TestVertice())
                .clear()
        assertThat(graph.elementsCount, `is`(0))
    }

    @Test
    fun shouldSetGraphElementId() {
        val v1 = TestVertice()
        val v2 = TestVertice()
        val graph = GraphImpl(mock<EventBus>())
        graph.add(v1)
        graph.add(v2)

        assertThat(v1.id, `is`(1))
        assertThat(v2.id, `is`(2))
    }

    @Test
    fun shouldBeStorable() {
        val testGraph = TestGraph(mock<EventBus>())
        val clone = IOModule.storableClonerProvider.invoke().clone(testGraph.graph) as Graph

        val v1: Vertice = clone.withId(1)!! as Vertice
        val v2: Vertice = clone.withId(2)!! as Vertice
        val net: Net<Boolean> = clone.withId(3)!! as Net<Boolean>
        assertThat(v1.getOutput<Boolean>().net!!.id, `is`(3))
        assertThat(net.isConnectedWith(v2.getInput()), `is`(true))
    }

    @Test
    fun shouldUnconnectEdgeWhenRemovingOriginVertice() {
        val testGraph = TestGraph(mock<EventBus>())

        testGraph.graph.remove(testGraph.v1)

        assertThat(testGraph.v1.getOutput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.net.isConnectedWith(testGraph.v1.getOutput()), `is`(false))
    }

    @Test
    fun shouldUnconnectEdgeWhenRemovingDestinationVertice() {
        val testGraph = TestGraph(mock<EventBus>())

        testGraph.graph.remove(testGraph.v2)

        assertThat(testGraph.v2.getInput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.net.isConnectedWith(testGraph.v2.getInput()), `is`(false))
    }

    @Test
    fun shouldUnconnectOutputPortWhenRemovingNet() {
        val testGraph = TestGraph(mock<EventBus>())

        testGraph.graph.remove(testGraph.net)

        assertThat(testGraph.v1.getOutput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.v2.getInput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.net.isConnectedWith(testGraph.v1.getOutput()), `is`(false))
        assertThat(testGraph.net.isConnectedWith(testGraph.v2.getInput()), `is`(false))
    }

    @Test
    fun shouldCreateUniqueGraphInputName() {
        val testGraph = GraphImpl(mock<EventBus>())

        val in1 = GraphInputImpl(PortImpl.createOutput(Boolean::class))
        testGraph.add(in1)
        val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class))
        testGraph.add(in2)

        assertThat(in1.name, `is`("I1"))
        assertThat(in2.name, `is`("I2"))
    }

    @Test
    fun shouldCreateUniqueGraphOutputName() {
        val testGraph = GraphImpl(mock<EventBus>())

        val out1 = GraphOutputImpl(PortImpl.createInput(Boolean::class))
        testGraph.add(out1)
        val out2 = GraphOutputImpl(PortImpl.createInput(Boolean::class))
        testGraph.add(out2)

        assertThat(out1.name, `is`("O1"))
        assertThat(out2.name, `is`("O2"))
    }

    @Test
    fun shouldCreateUniqueGraphInputOutputName() {
        val testGraph = GraphImpl(mock<EventBus>())

        val `in` = GraphInputImpl(PortImpl.createOutput(Boolean::class))
        testGraph.add(`in`)
        val out = GraphOutputImpl(PortImpl.createInput(Boolean::class))
        testGraph.add(out)

        assertThat(`in`.name, `is`("I1"))
        assertThat(out.name, `is`("O1"))
    }
}