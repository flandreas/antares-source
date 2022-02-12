package ch.scorpion.antares.filebased

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import ch.scorpion.antares.model.signal.DigitalSignalFactory.undefined
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals

class TriStateOutputTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var d1: Switch
	private lateinit var s1: Switch
	private lateinit var d2: Switch
	private lateinit var s2: Switch
	private lateinit var output: CircuitInOut

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("20143120-c058-4252-8c5c-af90257a60ba"))

		d1 = openedCircuitView.graph!!.withId(2) as Switch
		s1 = openedCircuitView.graph!!.withId(3) as Switch
		d2 = openedCircuitView.graph!!.withId(5) as Switch
		s2 = openedCircuitView.graph!!.withId(4) as Switch
		output = openedCircuitView.graph!!.withId(11) as CircuitInOut

		startSimulation()
		processUntilQueueIsEmpty()
	}

	@Test
	fun shouldOutputBeUndefinedAfterStart() {
		assertEquals(undefined(BitWidth.BW_1), output.signal)
	}

	@Test
	fun shouldActivateFirstSubGraph() {
		d1.toggle(scheduler)
		s1.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(of(True), output.signal)
	}

	@Test
	fun shouldOutputBeUndefinedAfterOneDeactivation() {
		d1.toggle(scheduler)
		s1.toggle(scheduler)
		processUntilQueueIsEmpty()
		s1.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(undefined(BitWidth.BW_1), output.signal)
	}

	@Ignore
	@Test
	fun shouldOutputBeUndefinedAfterTwoDeactivation() {
		d1.toggle(scheduler)
		s1.toggle(scheduler)
		d2.toggle(scheduler)
		s2.toggle(scheduler)
		processUntilQueueIsEmpty()
		assertEquals(of(True), output.signal)

		s1.toggle(scheduler)
		s2.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(undefined(BitWidth.BW_1), output.signal)
	}
}