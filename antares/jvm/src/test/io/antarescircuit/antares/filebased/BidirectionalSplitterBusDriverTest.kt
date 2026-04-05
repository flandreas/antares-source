package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.*

class BidirectionalSplitterBusDriverTest : AbstractFileBasedTest() {

	private lateinit var dirSwitch: Switch
	private lateinit var inOutA0: DigitalCircuitInOut
	private lateinit var inOutA1: DigitalCircuitInOut
	private lateinit var inOutB: DigitalCircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("4e24ce93-6521-4911-a8be-bce39ce6147a"))

		startSimulation()
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		dirSwitch = openedCircuitView.graph!!.withId(8) as Switch
		inOutA0 = openedCircuitView.graph!!.withId(3) as DigitalCircuitInOut
		inOutA1 = openedCircuitView.graph!!.withId(12) as DigitalCircuitInOut
		inOutB = openedCircuitView.graph!!.withId(4) as DigitalCircuitInOut
	}

	@AfterTest
	fun cleanup() {
		stopSimulation()
	}

	@Test
	fun shouldPropagateBit0() {
		dirSwitch.on(scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		inOutB.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.True, Bit.Undefined)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(DigitalSignalFactory.of(true), inOutA0.signal)
	}

	@Test
	fun shouldPropagateBit1() {
		dirSwitch.on(scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		inOutB.setIncomingSignal(DigitalSignalFactory.ofBits(listOf(Bit.Undefined, Bit.True)), scheduler)
		scheduler.proceedUntilQueueIsEmpty(timeService, actorListener)

		assertEquals(DigitalSignalFactory.of(true), inOutA1.signal)
	}
}