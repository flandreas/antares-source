package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.model.gate.TriStateBufferGate
import io.antarescircuit.antares.model.inout.DigitalCircuitInOut
import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.model.signal.Bit.True
import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_1
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory.of
import io.antarescircuit.antares.model.signal.DigitalSignalFactory.undefined
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TriStateOutputTest : AbstractFileBasedTest() {

	private lateinit var d1: Switch
	private lateinit var s1: Switch
	private lateinit var d2: Switch
	private lateinit var s2: Switch
	private lateinit var output: DigitalCircuitInOut
	private lateinit var ref1: SubGraphVerticeRef

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("20143120-c058-4252-8c5c-af90257a60ba"))

		ref1 = openedCircuitView.graph!!.withId(1) as SubGraphVerticeRef
		d1 = openedCircuitView.graph!!.withId(2) as Switch
		s1 = openedCircuitView.graph!!.withId(3) as Switch
		d2 = openedCircuitView.graph!!.withId(5) as Switch
		s2 = openedCircuitView.graph!!.withId(4) as Switch
		output = openedCircuitView.graph!!.withId(11) as DigitalCircuitInOut
	}

	@Test
	fun shouldOutputBeUndefinedAfterStartDeep() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeUndefinedAfterStart()
	}

	@Test
	fun shouldOutputBeUndefinedAfterStartScripted() {
		scheduler.isDeepExecution = false
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeUndefinedAfterStart()
	}

	private fun assertOutputBeUndefinedAfterStart() {
		assertEquals(undefined(BW_1), output.signal)
	}

	@Test
	fun shouldActivateFirstSubGraphDeep() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertActivateFirstSubGraph()
	}

	@Test
	fun shouldActivateFirstSubGraphScripted() {
		scheduler.isDeepExecution = false
		startSimulation()
		processUntilQueueIsEmpty()

		assertActivateFirstSubGraph()
	}

	private fun assertActivateFirstSubGraph() {
		d1.toggle(scheduler)
		s1.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(of(True), output.signal)
	}

	@Test
	fun shouldOutputBeUndefinedAfterOneDeactivationDeep() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeUndefinedAfterOneDeactivation()
	}

	@Test
	fun shouldOutputBeUndefinedAfterOneDeactivationScripted() {
		scheduler.isDeepExecution = false
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeUndefinedAfterOneDeactivation()
	}

	private fun assertOutputBeUndefinedAfterOneDeactivation() {
		d1.toggle(scheduler)
		s1.toggle(scheduler)
		processUntilQueueIsEmpty()
		s1.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(undefined(BW_1), output.signal)
	}

	@Test
	fun shouldOutputBeUndefinedAfterTwoDeactivationDeep() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeUndefinedAfterTwoDeactivation()
	}

	@Test
	fun shouldOutputBeUndefinedAfterTwoDeactivationScripted() {
		scheduler.isDeepExecution = false
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeUndefinedAfterTwoDeactivation()
	}

	private fun assertOutputBeUndefinedAfterTwoDeactivation() {
		d1.toggle(scheduler)
		s1.toggle(scheduler)
		d2.toggle(scheduler)
		s2.toggle(scheduler)
		processUntilQueueIsEmpty()
		assertEquals(of(True), output.signal)

		s1.toggle(scheduler)
		s2.toggle(scheduler)
		processUntilQueueIsEmpty()

		assertEquals(undefined(BW_1), output.signal)
	}

	@Test
	fun shouldOutputBeDefinedWithOnlyOneUndefinedSubcircuitDeep() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeDefinedWithOnlyOneUndefinedSubcircuit()
	}

	@Test
	fun shouldOutputBeDefinedWithOnlyOneUndefinedSubcircuitScripted() {
		scheduler.isDeepExecution = false
		startSimulation()
		processUntilQueueIsEmpty()

		assertOutputBeDefinedWithOnlyOneUndefinedSubcircuit()
	}

	private fun assertOutputBeDefinedWithOnlyOneUndefinedSubcircuit() {
		d1.toggle(scheduler)
		s1.toggle(scheduler)
		d2.toggle(scheduler)
		s2.toggle(scheduler)
		processUntilQueueIsEmpty()
		assertEquals(of(True), output.signal)

		s1.toggle(scheduler)
		processUntilQueueIsEmpty()

		val buffer = ref1.getGraphIfPresent()!!.withId(1) as TriStateBufferGate
		assertEquals(undefined(BW_1), buffer.getOutput<DigitalSignal>().net!!.signal)

		assertEquals(of(True), output.signal)
	}
}