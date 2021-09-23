package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.port.PortImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Unit tests for [AbstractVertice].
 */
class AbstractVerticeTest {

	private val vertice = MyVertice()

	companion object {
		init {
			GraphModelTestRule.configure()
		}
	}

	@Test
	fun shouldAccessInputsById() {
		vertice.addPort(input("A"))
		vertice.addPort(input("B"))

		assertEquals("A", vertice.getInput<Boolean>(1).name)
		assertEquals("B", vertice.getInput<Boolean>(2).name)
	}

	@Test
	fun shouldAccessOutputsById() {
		vertice.addPort(output("A"))
		vertice.addPort(output("B"))

		assertEquals("A", vertice.getOutput<Boolean>(1).name)
		assertEquals("B", vertice.getOutput<Boolean>(2).name)
	}

	@Test
	fun shouldProvideTypedPortAccessorsForGenericSignalAccess() {
		vertice.addPort(input("A"))
		vertice.addPort(output("B"))

		val signalIn: Boolean? = vertice.getInput<Boolean>("A").getIncomingSignal()
		assertNull(signalIn)

		val signalOut: Boolean? = vertice.getOutput<Boolean>("B").getOutgoingSignal()
		assertNull(signalOut)
	}

	@Test
	fun shouldProvidePortWithAnyType() {
		vertice.addPort(input("A"))
		vertice.addPort(output("B"))

		vertice.getInput<Any>("A")
		vertice.getOutput<Any>("B")
	}

	@Test
	fun shouldKeepPortIdUniqueWhenAddingAfterRemoving() {
		vertice.addPort(input("A"))
		vertice.addPort(input("B"))
		vertice.removePort(vertice.getPort<Boolean>("A"))
		vertice.addPort(input("C"))

		assertNotEquals(vertice.getPort<Boolean>("B").portId, vertice.getPort<Boolean>("C").portId)
	}

	private fun input(name: String): Port<Boolean> = PortImpl.createInput(name)

	private fun output(name: String): Port<Boolean> = PortImpl.createOutput(name)

	private class MyVertice : AbstractVertice("graph.property.label") {
		override val type: String get() = "MyVertice"
		override val typeDesc: String? get() = null
	}
}