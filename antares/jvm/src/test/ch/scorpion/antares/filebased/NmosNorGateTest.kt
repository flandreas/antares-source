package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class NmosNorGateTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var inputA: CircuitInOut
	private lateinit var inputB: CircuitInOut
	private lateinit var output: CircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("27b806ad-d3bb-4fb7-aaef-1cc193dbf78e"))

		startSimulation()
		processUntilQueueIsEmpty()

		inputA = openedCircuitView.graph!!.withId(3) as CircuitInOut
		inputB = openedCircuitView.graph!!.withId(4) as CircuitInOut
		output = openedCircuitView.graph!!.withId(13) as CircuitInOut
	}

	@Test
	fun shouldRemainOffWhenGoingFrom11To10() {
		inputA.setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		inputB.setIncomingSignal(DigitalSignalFactory.of(Bit.True), scheduler)
		processUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.of(Bit.False), output.signal)

		inputA.setIncomingSignal(DigitalSignalFactory.of(Bit.False), scheduler)
		processUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(Bit.False), output.signal)
	}
}