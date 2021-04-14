package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.TestGraphBuilder
import ch.scorpion.jabbah.graph.model.TestVerticeString
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.port.InconsistentNetError
import io.mockk.mockk
import kotlin.test.*

class CombinedNetTest {

	companion object {
		init {
			GraphModelTestRule.configure()
		}
	}

	private data class Structure(
		val a: Vertice,
		val b: Vertice,
		val transmitter: Vertice? = null,
		val b2: Vertice? = null
	)

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	@Test
	fun shouldBeEmptyWhenConnectedOnlyToInput() {
		val graph = buildOutputToInput()

		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		assertEquals(0, combinedNet.outputPorts.size)
	}

	@Test
	fun shouldContainImmediateOutputPort() {
		val graph = buildOutputToInput(bIsInOut = true)

		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		assertEquals(1, combinedNet.outputPorts.size)
		assertSame(graph.b.getOutput(2), combinedNet.outputPorts.first())
	}

	@Test
	fun shouldContainOutputPortsFromCombinedNet() {
		val graph = buildOutputToOutputViaTransmitter()

		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		assertEquals(1, combinedNet.outputPorts.size)
		assertTrue(combinedNet.outputPorts.contains(graph.b.getOutput()))
	}

	@Test
	fun shouldBeConsistentWithTransformedSignal() {
		val graph = buildOutputToOutputViaTransmitter(object : SignalConverter<String> {
			override fun convert(signal: String?): String = "${signal}T"
		})
		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		graph.a.getOutput<String>(2).setOutgoingSignalBuffered("A", signalHandler)
		graph.b.getOutput<String>(2).setOutgoingSignalBuffered("AT", signalHandler)

		assertNull(combinedNet.checkForConflict(graph.a.getOutput(2)))
	}

	@Test
	fun shouldBeConsistentWithAllOthersUndefined() {
		val graph = buildOutputToOutputViaTransmitter()
		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		graph.a.getOutput<String>(2).setOutgoingSignalBuffered("A", signalHandler)
		graph.b.getOutput<String>(2).setOutgoingSignalBuffered(null, signalHandler)

		assertNull(combinedNet.checkForConflict(graph.a.getOutput(2)))
	}

	@Test
	fun shouldNotBeConsistentWithDifferentOutput() {
		val graph = buildOutputToOutputViaTransmitter()
		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		graph.a.getOutput<String>(2).setOutgoingSignalBuffered("A", signalHandler)
		graph.b.getOutput<String>(2).setOutgoingSignalBuffered("Bla", signalHandler)

		assertNotNull(combinedNet.checkForConflict(graph.a.getOutput(2)))
	}

	@Test
	fun shouldOnlyDefinedPortsBeConsistent() {
		val graph = buildOutputToInput(bIsInOut = true)
		graph.b.getOutput<String>(2).setOutgoingSignalBuffered("B", signalHandler)
		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		val consistentPort = combinedNet.consistentSignalPort

		assertSame(graph.b.getOutput<String>(2), consistentPort)
	}

	@Test
	fun shouldNotFindConsistentPortWithDifferentSignals() {
		val graph = buildOutputToInput(bIsInOut = true, secondB = true)
		graph.b.getOutput<String>(2).setOutgoingSignalBuffered("B", signalHandler)
		graph.b2!!.getOutput<String>(2).setOutgoingSignalBuffered("B2", signalHandler)
		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)

		val consistentPort = combinedNet.consistentSignalPort

		assertNull(consistentPort)
	}

	@Test
	fun shouldSetExecutionError() {
		val graph = buildOutputToOutputViaTransmitter()
		val combinedNet = CombinedNet.fromOutputPort(graph.a.getOutput<String>(), signalHandler)
		val error = InconsistentNetError(graph.a.getOutput<String>(), combinedNet, SignalConflict(true, graph.b.getOutput()))

		combinedNet.setExecutionError(error)

		assertSame(error, graph.a.getOutput<String>().net!!.executionError)
		assertSame(error, graph.b.getOutput<String>().net!!.executionError)
	}

	private fun buildOutputToInput(bIsInOut: Boolean = false, secondB: Boolean = false): Structure {
		val builder = TestGraphBuilder<String>()
		val a = builder.addVertice(TestVerticeString())
		val b = builder.addVertice(TestVerticeString(outputIsInOut = bIsInOut))
		val b2 = if (secondB) builder.addVertice(TestVerticeString(outputIsInOut = bIsInOut)) else null
		if (bIsInOut) {
			val net = builder.connect(a, a.getOutput(), b, b.getInput(2))
			b2?.let {
				net.connect(it.getInput(2))
			}
		} else {
			val net = builder.connect(a, a.getOutput(), b, b.getInput())
			b2?.let {
				net.connect(it.getOutput())
			}
		}

		return Structure(a, b, b2 = b2)
	}

	private fun buildOutputToOutputViaTransmitter(converter: SignalConverter<String>? = null): Structure {
		val builder = TestGraphBuilder<String>()
		val a = builder.addVertice(TestVerticeString())
		val transmitter = builder.addVertice(TestVerticeString(outputIsInOut = true, converter = converter))
		val b = builder.addVertice(TestVerticeString())
		builder.connect(a, to = transmitter, toPort = transmitter.getInput(1))
		builder.connect(b, fromPort = b.getOutput(2), to = transmitter, toPort = transmitter.getInput(2))

		return Structure(a, b, transmitter)
	}
}