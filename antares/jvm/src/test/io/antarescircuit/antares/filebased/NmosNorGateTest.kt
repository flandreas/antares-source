package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.UUID
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class NmosNorGateTest : AbstractFileBasedTest() {

	private lateinit var inputA: DigitalCircuitInOut
	private lateinit var inputB: DigitalCircuitInOut
	private lateinit var output: DigitalCircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("27b806ad-d3bb-4fb7-aaef-1cc193dbf78e"))

		startSimulation()
		processUntilQueueIsEmpty()

		inputA = openedCircuitView.graph!!.withId(3) as DigitalCircuitInOut
		inputB = openedCircuitView.graph!!.withId(4) as DigitalCircuitInOut
		output = openedCircuitView.graph!!.withId(13) as DigitalCircuitInOut
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