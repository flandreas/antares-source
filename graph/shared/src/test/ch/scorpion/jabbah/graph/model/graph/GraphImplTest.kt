package ch.scorpion.jabbah.graph.model.graph

import com.nhaarman.mockitokotlin2.mock
import ch.scorpion.jabbah.base.event.EventBusImpl
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
        val graph = GraphImpl(mock())
                .add(TestVertice())
                .add(TestVertice())
                .clear()
        assertThat(graph.elementsCount, `is`(0))
    }

    @Test
    fun shouldSetGraphElementId() {
        val v1 = TestVertice()
        val v2 = TestVertice()
        val graph = GraphImpl(mock())
        graph.add(v1)
        graph.add(v2)

        assertThat(v1.id, `is`(1))
        assertThat(v2.id, `is`(2))
    }

    @Test
    fun shouldBeStorable() {
        val testGraph = TestGraph(mock())
        val clone = IOModule.storableClonerProvider.invoke().clone(testGraph.graph) as Graph

        val v1: Vertice = clone.withId(1)!! as Vertice
        val v2: Vertice = clone.withId(2)!! as Vertice
        val net: Net<Boolean> = clone.withId(3)!! as Net<Boolean>
        assertThat(v1.getOutput<Boolean>().net!!.id, `is`(3))
        assertThat(net.isConnectedWith(v2.getInput()), `is`(true))
    }

    @Test
    fun shouldUnconnectEdgeWhenRemovingOriginVertice() {
        val testGraph = TestGraph(mock())

        testGraph.graph.remove(testGraph.v1)

        assertThat(testGraph.v1.getOutput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.net.isConnectedWith(testGraph.v1.getOutput()), `is`(false))
    }

    @Test
    fun shouldUnconnectEdgeWhenRemovingDestinationVertice() {
        val testGraph = TestGraph(mock())

        testGraph.graph.remove(testGraph.v2)

        assertThat(testGraph.v2.getInput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.net.isConnectedWith(testGraph.v2.getInput()), `is`(false))
    }

    @Test
    fun shouldUnconnectOutputPortWhenRemovingNet() {
        val testGraph = TestGraph(mock())

        testGraph.graph.remove(testGraph.net)

        assertThat(testGraph.v1.getOutput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.v2.getInput<Boolean>().net, `is`(nullValue()))
        assertThat(testGraph.net.isConnectedWith(testGraph.v1.getOutput()), `is`(false))
        assertThat(testGraph.net.isConnectedWith(testGraph.v2.getInput()), `is`(false))
    }

    @Test
    fun shouldCreateUniqueGraphInputName() {
        val testGraph = GraphImpl(mock())

        val in1 = GraphInputImpl(PortImpl.createOutput(Boolean::class))
        testGraph.add(in1)
        val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class))
        testGraph.add(in2)

        assertThat(in1.name, `is`("I1"))
        assertThat(in2.name, `is`("I2"))
    }

	@Test
	fun shouldCreateUniqueGraphInputNameForExisting() {
		val testGraph = GraphImpl(mock())

		val in1 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1")
		testGraph.add(in1)
		val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1")
		testGraph.add(in2)

		assertThat(in1.name, `is`("I1"))
		assertThat(in2.name, `is`("I2"))
	}

    @Test
    fun shouldCreateUniqueGraphOutputName() {
        val testGraph = GraphImpl(mock())

        val out1 = GraphOutputImpl(PortImpl.createInput(Boolean::class))
        testGraph.add(out1)
        val out2 = GraphOutputImpl(PortImpl.createInput(Boolean::class))
        testGraph.add(out2)

        assertThat(out1.name, `is`("O1"))
        assertThat(out2.name, `is`("O2"))
    }

    @Test
    fun shouldCreateUniqueGraphInputOutputName() {
        val testGraph = GraphImpl(mock())

        val `in` = GraphInputImpl(PortImpl.createOutput(Boolean::class))
        testGraph.add(`in`)
        val out = GraphOutputImpl(PortImpl.createInput(Boolean::class))
        testGraph.add(out)

        assertThat(`in`.name, `is`("I1"))
        assertThat(out.name, `is`("O1"))
    }

	@Test
    fun shouldNotChangeUniqueInputName() {
		val testGraph = GraphImpl(eventBus = EventBusImpl())
		val `in` = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I99")

		testGraph.add(`in`)

		assertThat(`in`.name, `is`("I99"))
    }

	@Test
	fun shouldNotChangeUniqueOutputName() {
		val testGraph = GraphImpl(eventBus = EventBusImpl())
		val out = GraphInputImpl(PortImpl.createInput(Boolean::class), "O99")

		testGraph.add(out)

		assertThat(out.name, `is`("O99"))
	}

	@Test
	fun shouldNotChangeUniqueInOutName() {
		val testGraph = GraphImpl(eventBus = EventBusImpl())
		val inout = GraphInputImpl(PortImpl.createInOut(Boolean::class), "IO99")

		testGraph.add(inout)

		assertThat(inout.name, `is`("IO99"))
	}

	@Test
	fun shouldAllowUniquePortNameChange () {
		val eventBus = EventBusImpl()
		val testGraph = GraphImpl(eventBus = eventBus)
		testGraph.add(GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1", eventBus))
		val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I2", eventBus)
		testGraph.add(in2)

		in2.name = "I3"

		assertThat(in2.name, `is`("I3"))
	}

	@Test
	fun shouldPreventNonUniquePortNameChange() {
		val eventBus = EventBusImpl()
		val testGraph = GraphImpl(eventBus = eventBus)
		testGraph.add(GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1", eventBus))
		val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I2", eventBus)
		testGraph.add(in2)

		in2.name = "I1"

		assertThat(in2.name, `is`("I2"))
	}
}