package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import kotlin.test.*

class Transceiver8BitTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var a: CircuitInOut
	private lateinit var transceiverA: SubGraphVerticeRef
	private lateinit var transceiverB: SubGraphVerticeRef
	private lateinit var transceiverC: SubGraphVerticeRef

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("3dafc474-8fcb-4440-8dbc-70b947fcd2be"))

		a = openedCircuitView.graph!!.withId(2) as CircuitInOut
		transceiverA = openedCircuitView.graph!!.withId(3) as SubGraphVerticeRef
		transceiverB = openedCircuitView.graph!!.withId(5) as SubGraphVerticeRef
		transceiverC = openedCircuitView.graph!!.withId(9) as SubGraphVerticeRef

		startSimulation()
	}

	@Test
	fun combinedNetOfAShouldReachToTriStateOutput() {
		val combinedNets = a.getOutput<DigitalSignal>().combinedNets

		assertEquals(8, combinedNets.size)

		combinedNets.forEach { combinedNet ->
			assertEquals(2, combinedNet.accesses.size)
			combinedNet.accesses
				.first { it.port !== a.getOutput<DigitalSignal>() }
				.run {
					assertTrue(port.owner is TriStateBufferGate)
				}
		}
	}

	@Test
	fun combinedNetOfTriStateGateAShouldReachToTriStateGateOfB() {
		val innerTransceiverA0 = transceiverA.getGraphIfPresent()!!.withId(1) as SubGraphVerticeRef
		val innerTransceiverB0 = transceiverB.getGraphIfPresent()!!.withId(1) as SubGraphVerticeRef
		val triStateA0 = innerTransceiverA0.getGraphIfPresent()!!.withId(11) as TriStateBufferGate
		val triStateB0 = innerTransceiverB0.getGraphIfPresent()!!.withId(12) as TriStateBufferGate

		val combinedNets = triStateA0.getOutputPort().combinedNets
		assertEquals(1, combinedNets.size)

		val accesses = combinedNets.iterator().next().accesses
		assertEquals(2, accesses.size)

		accesses
			.first { it.port !== triStateA0.getOutput<DigitalSignal>() }
			.run {
				assertSame(port.owner, triStateB0)
			}
	}
}