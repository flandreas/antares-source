package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.Bit.Undefined
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WireTapTest : AbstractFileBasedTest() {

	private lateinit var a: DigitalCircuitInOut
	private lateinit var b: DigitalCircuitInOut
	private lateinit var t0: DigitalCircuitInOut
	private lateinit var t1: DigitalCircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("e147b930-31b7-4234-8b71-4e073fc09da9"))

		a = openedCircuitView.graph!!.withId(1) as DigitalCircuitInOut
		b = openedCircuitView.graph!!.withId(2) as DigitalCircuitInOut
		t0 = openedCircuitView.graph!!.withId(5) as DigitalCircuitInOut
		t1 = openedCircuitView.graph!!.withId(6) as DigitalCircuitInOut

		startSimulation()
	}

	@Test
	fun shouldForwardOddlyTappedSignals() {
		t0.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(True, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		t0.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Undefined, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		a.setIncomingSignal(DigitalSignalFactory.allOf(BitWidth.BW_8, Undefined).withBit(6, True), scheduler)
		proceedUntilQueueIsEmpty()

		a.setIncomingSignal(DigitalSignalFactory.allOf(BitWidth.BW_8, Undefined), scheduler)
		proceedUntilQueueIsEmpty()

		// Again the same as in step 1
		t0.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(True, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_8, Undefined).withBit(1, True), a.signal)
	}
}