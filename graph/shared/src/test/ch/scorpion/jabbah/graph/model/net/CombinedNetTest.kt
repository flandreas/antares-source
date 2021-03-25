package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.TestGraphBuilder
import ch.scorpion.jabbah.graph.model.TestVertice
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

	private val signalHandler = mockk<SignalHandler>(relaxed = true)

	private data class Structure(
		val input: Vertice,
		val output: Vertice,
		val output2: Vertice? = null,
		val transmitter: Vertice? = null
	)

	@Test
	fun shouldNotContainInputPorts() {
		val graph = buildInputToOutput()

		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())

		assertEquals(0, combinedNet.outputPorts.size)
	}

	@Test
	fun shouldContainOutputPortsFromSameNet() {
		val graph = buildInputToOutput(outputInOut = true)

		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())

		assertEquals(1, combinedNet.outputPorts.size)
		assertSame(graph.output.getOutput<Boolean>(2), combinedNet.outputPorts[0])
	}

	@Test
	fun shouldContainOutputPortsFromCombinedNet() {
		val graph = buildInputToOutputViaTransmitter()

		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())

		assertEquals(2, combinedNet.outputPorts.size)
		assertTrue(combinedNet.outputPorts.contains(graph.transmitter!!.getOutput<Boolean>(2)))
		assertTrue(combinedNet.outputPorts.contains(graph.output.getOutput<Boolean>(2)))
	}

	@Test
	fun shouldBeConsistentWithAllOthersUndefined() {
		val graph = buildInputToOutput(outputInOut = true)

		graph.input.getOutput<Boolean>().setOutgoingSignalBuffered(true, signalHandler)
		graph.output.getOutput<Boolean>().setOutgoingSignalBuffered(null, signalHandler)

		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())

		assertTrue(combinedNet.isConsistentWith((graph.input.getOutput<Boolean>())))
	}

	@Test
	fun shouldNotBeConsistentWithOtherDefinedDifferently() {
		val graph = buildInputToOutput(outputInOut = true)

		graph.input.getOutput<Boolean>().setOutgoingSignalBuffered(true, signalHandler)
		graph.output.getOutput<Boolean>().setOutgoingSignalBuffered(false, signalHandler)

		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())

		assertFalse(combinedNet.isConsistentWith((graph.input.getOutput<Boolean>())))
	}

	@Test
	fun shouldOnlyDefinedPortBeConsistent() {
		val graph = buildInputToOutput(outputInOut = true)
		graph.output.getOutput<Boolean>().setOutgoingSignalBuffered(false, signalHandler)
		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())

		val consistentPort = combinedNet.consistentSignalPort

		assertSame(graph.output.getOutput<Boolean>(), consistentPort)
	}

	@Test
	fun shouldNotFindConsistentPortWithDifferentSignals() {
		val graph = buildInputToOutput(outputInOut = true, secondOutput = true)
		graph.output.getOutput<Boolean>().setOutgoingSignalBuffered(false, signalHandler)
		graph.output2!!.getOutput<Boolean>().setOutgoingSignalBuffered(true, signalHandler)
		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())

		val consistentPort = combinedNet.consistentSignalPort

		assertNull(consistentPort)
	}

	@Test
	fun shouldSetExecutionError() {
		val graph = buildInputToOutputViaTransmitter()
		val combinedNet = CombinedNet.fromOutputPort(graph.input.getOutput<Boolean>())
		val error = InconsistentNetError()

		combinedNet.setExecutionError(error)

		assertSame(error, graph.input.getOutput<Boolean>().net!!.executionError)
		assertSame(error, graph.output.getOutput<Boolean>().net!!.executionError)
	}

	private fun buildInputToOutput(outputInOut: Boolean = false, secondOutput: Boolean = false): Structure {
		val builder = TestGraphBuilder<Boolean>()
		val input = builder.addVertice(TestVertice())
		val output = builder.addVertice(TestVertice(inOut = outputInOut))
		val output2 = if (secondOutput) builder.addVertice(TestVertice(inOut = outputInOut)) else null
		if (outputInOut) {
			val net =builder.connect(input, to = output, toPort = output.getInput(2))
			output2?.let {
				net.connect(it.getInput(2))
			}
		} else {
			val net = builder.connect(input, to = output)
			output2?.let {
				net.connect(it.getOutput())
			}
		}

		return Structure(input, output, output2)
	}

	private fun buildInputToOutputViaTransmitter(): Structure {
		val builder = TestGraphBuilder<Boolean>()
		val input = builder.addVertice(TestVertice())
		val transmitter = builder.addVertice(TestVertice(inOut = true))
		val output = builder.addVertice(TestVertice(inOut = true))
		builder.connect(input, to = transmitter, toPort = transmitter.getInput(2))
		builder.connect(output, fromPort = output.getOutput(2), transmitter, transmitter.getInput(1))

		return Structure(input, output, null, transmitter)
	}
}