package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.port.PortImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphInputImpl
import ch.scorpion.jabbah.graph.model.vertice.GraphOutputImpl
import ch.scorpion.jabbah.io.IOModule
import io.mockk.mockk
import kotlin.test.*

/**
 * Unit tests for [GraphImpl].
 */
class GraphImplTest {

	companion object {
		init {
			GraphModelTestRule.configure()
		}
	}

	@Test
	fun shouldClear() {
		val graph = GraphImpl(eventBus = mockk(relaxed = true))
			.add(TestVertice())
			.add(TestVertice())
			.clear()
		assertEquals(0, graph.elementsCount)
	}

	@Test
	fun shouldSetGraphElementId() {
		val v1 = TestVertice()
		val v2 = TestVertice()
		val graph = GraphImpl(eventBus = mockk(relaxed = true))
		graph.add(v1)
		graph.add(v2)

		assertEquals(1, v1.id)
		assertEquals(2, v2.id)
	}

	@Test
	fun shouldBeStorable() {
		val testGraph = TestGraph(eventBus = mockk(relaxed = true))
			val clone = IOModule . storableClonerProvider . invoke ().clone(testGraph.graph) as Graph

		val v1: Vertice = clone.withId(1)!! as Vertice
		val v2: Vertice = clone.withId(2)!! as Vertice
		val net: Net<Boolean> = clone.withId(3)!! as Net<Boolean>
		assertEquals(3, v1.getOutput<Boolean>().net!!.id)
		assertTrue(net.isConnectedWith(v2.getInput()))
	}

	@Test
	fun shouldUnconnectEdgeWhenRemovingOriginVertice() {
		val testGraph = TestGraph(eventBus = mockk(relaxed = true))

		testGraph.graph.remove(testGraph.v1)

		assertNull(testGraph.v1.getOutput<Boolean>().net)
		assertFalse(testGraph.net.isConnectedWith(testGraph.v1.getOutput()))
	}

	@Test
	fun shouldUnconnectEdgeWhenRemovingDestinationVertice() {
		val testGraph = TestGraph(eventBus = mockk(relaxed = true))

		testGraph.graph.remove(testGraph.v2)

		assertNull(testGraph.v2.getInput<Boolean>().net)
		assertFalse(testGraph.net.isConnectedWith(testGraph.v2.getInput()))
	}

	@Test
	fun shouldUnconnectOutputPortWhenRemovingNet() {
		val testGraph = TestGraph(eventBus = mockk(relaxed = true))

		testGraph.graph.remove(testGraph.net)

		assertNull(testGraph.v1.getOutput<Boolean>().net)
		assertNull(testGraph.v2.getInput<Boolean>().net)
		assertFalse(testGraph.net.isConnectedWith(testGraph.v1.getOutput()))
		assertFalse(testGraph.net.isConnectedWith(testGraph.v2.getInput()))
	}

	@Test
	fun shouldCreateUniqueGraphInputName() {
		val testGraph = GraphImpl(eventBus = mockk(relaxed = true))

		val in1 = GraphInputImpl(PortImpl.createOutput(Boolean::class))
		testGraph.add(in1)
		val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class))
		testGraph.add(in2)

		assertEquals("I1", in1.name)
		assertEquals("I2", in2.name)
	}

	@Test
	fun shouldCreateUniqueGraphInputNameForExisting() {
		val testGraph = GraphImpl(eventBus = mockk(relaxed = true))

		val in1 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1")
		testGraph.add(in1)
		val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1")
		testGraph.add(in2)

		assertEquals("I1", in1.name)
		assertEquals("I2", in2.name)
	}

	@Test
	fun shouldCreateUniqueGraphOutputName() {
		val testGraph = GraphImpl(eventBus = mockk(relaxed = true))

		val out1 = GraphOutputImpl(PortImpl.createInput(Boolean::class))
		testGraph.add(out1)
		val out2 = GraphOutputImpl(PortImpl.createInput(Boolean::class))
		testGraph.add(out2)

		assertEquals("O1", out1.name)
		assertEquals("O2", out2.name)
	}

	@Test
	fun shouldCreateUniqueGraphInputOutputName() {
		val testGraph = GraphImpl(eventBus = mockk(relaxed = true))

		val `in` = GraphInputImpl(PortImpl.createOutput(Boolean::class))
		testGraph.add(`in`)
		val out = GraphOutputImpl(PortImpl.createInput(Boolean::class))
		testGraph.add(out)

		assertEquals("I1", `in`.name)
		assertEquals("O1", out.name)
	}

	@Test
	fun shouldNotChangeUniqueInputName() {
		val testGraph = GraphImpl(eventBus = EventBusImpl())
		val `in` = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I99")

		testGraph.add(`in`)

		assertEquals("I99", `in`.name)
	}

	@Test
	fun shouldNotChangeUniqueOutputName() {
		val testGraph = GraphImpl(eventBus = EventBusImpl())
		val out = GraphInputImpl(PortImpl.createInput(Boolean::class), "O99")

		testGraph.add(out)

		assertEquals("O99", out.name)
	}

	@Test
	fun shouldNotChangeUniqueInOutName() {
		val testGraph = GraphImpl(eventBus = EventBusImpl())
		val inout = GraphInputImpl(PortImpl.createInOut(Boolean::class), "IO99")

		testGraph.add(inout)

		assertEquals("IO99", inout.name)
	}

	@Test
	fun shouldAllowUniquePortNameChange() {
		val eventBus = EventBusImpl()
		val testGraph = GraphImpl(eventBus = eventBus)
		testGraph.add(GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1", eventBus))
		val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I2", eventBus)
		testGraph.add(in2)

		in2.name = "I3"

		assertEquals("I3", in2.name)
	}

	@Test
	fun shouldPreventNonUniquePortNameChange() {
		val eventBus = EventBusImpl()
		val testGraph = GraphImpl(eventBus = eventBus)
		testGraph.add(GraphInputImpl(PortImpl.createOutput(Boolean::class), "I1", eventBus))
		val in2 = GraphInputImpl(PortImpl.createOutput(Boolean::class), "I2", eventBus)
		testGraph.add(in2)

		in2.name = "I1"

		assertEquals("I2", in2.name)
	}
}