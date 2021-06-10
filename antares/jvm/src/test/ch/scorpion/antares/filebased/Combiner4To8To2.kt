package ch.scorpion.antares.filebased

import ch.scorpion.antares.checkCombinedNetAccess
import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Combiner4To8To2 : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var a0: CircuitInOut
	private lateinit var a1: CircuitInOut
	private lateinit var b0: CircuitInOut
	private lateinit var b1: CircuitInOut
	private lateinit var b2: CircuitInOut
	private lateinit var b3: CircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("d06b4df8-ed84-4af2-8b39-1252c7c4ca14"))

		a0 = openedCircuitView.graph!!.withId(4) as CircuitInOut
		a1 = openedCircuitView.graph!!.withId(5) as CircuitInOut
		b0 = openedCircuitView.graph!!.withId(8) as CircuitInOut
		b1 = openedCircuitView.graph!!.withId(9) as CircuitInOut
		b2 = openedCircuitView.graph!!.withId(10) as CircuitInOut
		b3 = openedCircuitView.graph!!.withId(11) as CircuitInOut

		startSimulation()
	}

	@Test
	fun shouldCreateCombinedTestsOfB0() {
		val combinedNets = b0.getOutput<DigitalSignal>().combinedNets

		assertEquals(1, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.iterator().next(),
			b0.getOutput(), BitWidth.BW_2, 0,
			a0.getOutput(), BitWidth.BW_2, 0)
	}

	@Test
	fun shouldCreateCombinedTestsOfB1() {
		val combinedNets = b1.getOutput<DigitalSignal>().combinedNets

		assertEquals(1, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.iterator().next(),
			b1.getOutput(), BitWidth.BW_2, 0,
			a0.getOutput(), BitWidth.BW_2, 1)
	}

	@Test
	fun shouldCreateCombinedTestsOfB2() {
		val combinedNets = b2.getOutput<DigitalSignal>().combinedNets

		assertEquals(1, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.iterator().next(),
			b2.getOutput(), BitWidth.BW_2, 0,
			a1.getOutput(), BitWidth.BW_2, 0)
	}

	@Test
	fun shouldCreateCombinedTestsOfB3() {
		val combinedNets = b3.getOutput<DigitalSignal>().combinedNets

		assertEquals(1, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.iterator().next(),
			b3.getOutput(), BitWidth.BW_2, 0,
			a1.getOutput(), BitWidth.BW_2, 1)
	}
}