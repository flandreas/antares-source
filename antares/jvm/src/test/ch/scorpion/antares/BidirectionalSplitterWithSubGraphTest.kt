package ch.scorpion.antares

import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BidirectionalSplitterWithSubGraphTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var dirSwitch: Switch
	private lateinit var inOutA0: CircuitInOut
	private lateinit var inOutA1: CircuitInOut
	private lateinit var inOutB: CircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("4e24ce93-6521-4911-a8be-bce39ce6147a"))

		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		dirSwitch = openedCircuitView.graph!!.withId(8) as Switch
		inOutA0 = openedCircuitView.graph!!.withId(3) as CircuitInOut
		inOutA1 = openedCircuitView.graph!!.withId(12) as CircuitInOut
		inOutB = openedCircuitView.graph!!.withId(4) as CircuitInOut
	}

	@AfterTest
	fun cleanup() {
		stopSimulation()
	}

	@Test
	fun checkLengthOfChainBit0() {
		val combinedNet = openedCircuitView.graph!!.getGraphPort<DigitalSignal>("B")!!.getOutput<DigitalSignal>().combinedNet!!

		val busDriverA0 = openedCircuitView.graph!!.withId(1) as SubGraphVerticeRef
		val triStateBufferA0 = busDriverA0.getGraphIfPresent()!!.withId(4) as TriStateBufferGate
		val chain = combinedNet.getChainTo(triStateBufferA0.getOutputPort())

		assertEquals(1, chain!!.convertersCount)
	}

	@Test
	fun checkLengthOfChainBit1() {
		val combinedNet = openedCircuitView.graph!!.getGraphPort<DigitalSignal>("B")!!.getOutput<DigitalSignal>().combinedNet!!

		val busDriverA1 = openedCircuitView.graph!!.withId(10) as SubGraphVerticeRef
		val triStateBufferA1 = busDriverA1.getGraphIfPresent()!!.withId(4) as TriStateBufferGate
		val chain = combinedNet.getChainTo(triStateBufferA1.getOutputPort())

		assertEquals(1, chain!!.convertersCount)
	}

	@Test
	fun shouldPropagateBit0() {
		dirSwitch.on(scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		inOutB.setIncomingSignal(Word(listOf(Bit.True, Bit.Undefined)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(true), inOutA0.signal)
	}

	@Test
	fun shouldPropagateBit1() {
		dirSwitch.on(scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		val splitter = openedCircuitView.graph!!.withId(2)

		inOutB.setIncomingSignal(Word(listOf(Bit.Undefined, Bit.True)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(Word.of(true), inOutA1.signal)
	}
}