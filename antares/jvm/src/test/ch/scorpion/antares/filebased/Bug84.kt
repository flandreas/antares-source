package ch.scorpion.antares.filebased

import ch.scorpion.antares.checkCombinedNetAccess
import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.input.Keyboard
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression test for github bug issue #84 regarding bi-directional multi-level splitting logic. */
class Bug84 : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var keyboard: Keyboard
	private lateinit var transceiver8: SubGraphVerticeRef
	private lateinit var b: CircuitInOut

	private lateinit var highTransceiver4: SubGraphVerticeRef
	private lateinit var triStateBuffer: TriStateBufferGate

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("1edd94d0-0f5c-467d-aace-b5e80638e431"))

		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		keyboard = openedCircuitView.graph!!.withId(14) as Keyboard
		transceiver8 = openedCircuitView.graph!!.withId(2) as SubGraphVerticeRef
		b = openedCircuitView.graph!!.withId(10) as CircuitInOut

		highTransceiver4 = transceiver8.getGraphIfPresent()!!.withId(2) as SubGraphVerticeRef
		triStateBuffer = highTransceiver4.getGraphIfPresent()!!.withId(22) as TriStateBufferGate
	}

	@Test
	fun shouldBuildCombinedAccessOfTriStateBufferGate() {
		val combinedNets = CombinedNet.createFor<DigitalSignal>(triStateBuffer.getOutput(3), scheduler)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(triStateBuffer.getOutput(3)) != null },
			keyboard.dataOutput, BitWidth.BW_1, 5,
			triStateBuffer.getOutput(3), BitWidth.BW_1, 0
		)
	}

	@Test
	fun shouldPropagateFromAToB() {
		keyboard.enter('a'.code.toByte(), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(BitWidth.BW_8, 'a'.code.toLong()), b.signal)
	}
}