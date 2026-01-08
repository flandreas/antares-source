package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.gate.TriStateBufferGateView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Two cross-coupled [TriStateBufferGateView]s, both constantly disabled.
 */
class TriStateDisabledTest : AbstractFileBasedTest() {

	private lateinit var inout1: DigitalCircuitInOutView
	private lateinit var inout2: DigitalCircuitInOutView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("6f96b4ff-55dd-4f16-bba7-aa0996b94bc0"))

		inout1 = openedCircuitView.getWithId(3) as DigitalCircuitInOutView
		inout2 = openedCircuitView.getWithId(5) as DigitalCircuitInOutView
	}

	@Test
	fun shouldNotOscillateAtStartup() {
		startSimulation()
		proceedUntilQueueIsEmpty()
	}

	@Test
	fun shouldNotPropagateSignals() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		inout1.model.toggleBit(0, undefine = false, scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.trueValue(BitWidth.BW_1), inout1.model.signal)
		assertEquals(DigitalSignalFactory.undefined(BitWidth.BW_1), inout2.model.signal)

		inout2.model.toggleBit(0, undefine = false, scheduler)
		proceedUntilQueueIsEmpty()
		assertEquals(DigitalSignalFactory.trueValue(BitWidth.BW_1), inout1.model.signal)
		assertEquals(DigitalSignalFactory.trueValue(BitWidth.BW_1), inout2.model.signal)
	}
}