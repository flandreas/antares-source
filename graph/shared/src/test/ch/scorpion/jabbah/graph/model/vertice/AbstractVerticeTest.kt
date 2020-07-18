package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.port.PortImpl
import kotlin.test.Test
import kotlin.test.assertEquals
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
		vertice.addPort(PortImpl.createInput(Boolean::class, "A"))
		vertice.addPort(PortImpl.createInput(Boolean::class, "B"))

		assertEquals("A", vertice.getInput<Boolean>(1).name)
		assertEquals("B", vertice.getInput<Boolean>(2).name)
	}

	@Test
	fun shouldAccessOutputsById() {
		vertice.addPort(PortImpl.createOutput(Boolean::class, "A"))
		vertice.addPort(PortImpl.createOutput(Boolean::class, "B"))

		assertEquals("A", vertice.getOutput<Boolean>(1).name)
		assertEquals("B", vertice.getOutput<Boolean>(2).name)
	}

	@Test
	fun shouldProvideTypedPortAccessorsForGenericSignalAccess() {
		vertice.addPort(PortImpl.createInput(Boolean::class, "A"))
		vertice.addPort(PortImpl.createOutput(Boolean::class, "B"))

		val signalIn: Boolean? = vertice.getInput<Boolean>("A").getIncomingSignal()
		assertNull(signalIn)

		val signalOut: Boolean? = vertice.getOutput<Boolean>("B").getOutgoingSignal()
		assertNull(signalOut)
	}

	@Test
	fun shouldProvidePortWithAnyType() {
		vertice.addPort(PortImpl.createInput(Boolean::class, "A"))
		vertice.addPort(PortImpl.createOutput(Boolean::class, "B"))

		vertice.getInput<Any>("A")
		vertice.getOutput<Any>("B")
	}

	private class MyVertice : AbstractVertice("graph.property.label") {
		override val type: String get() = "MyVertice"
		override val typeDesc: String? get() = null
	}
}