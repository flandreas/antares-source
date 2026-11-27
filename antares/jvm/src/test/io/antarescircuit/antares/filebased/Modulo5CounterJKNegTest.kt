package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Modulo5CounterJKNegTest : AbstractFileBasedTest() {

	private lateinit var input: DigitalCircuitInOutView
	private lateinit var output: DigitalCircuitInOutView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("f32ec46c-c2b1-4c19-8049-b45b9edd09e4"))

		input = openedCircuitView.getWithId(56) as DigitalCircuitInOutView
		output = openedCircuitView.getWithId(52) as DigitalCircuitInOutView
	}

	@Test
	fun shouldResetAt5Deep() {
		shouldResetAt5()
	}

	@Test
	fun shouldResetAt5Scripted() {
		scheduler.isDeepExecution = false
		shouldResetAt5()
	}

	private fun shouldResetAt5() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertOutput(0UL)
		repeat(3) {
			assertClockUpDownTo(1UL)
			assertClockUpDownTo(2UL)
			assertClockUpDownTo(3UL)
			assertClockUpDownTo(4UL)
			assertClockUpDownTo(0UL)
		}
	}

	private fun assertClockUpDownTo(value: ULong) {
		clockUpDown()
		assertOutput(value)
	}

	private fun assertOutput(value: ULong) {
		assertEquals(value, output.model.signal!!.getValue())
	}

	private fun clockUpDown() {
		input.model.toggleBit(0, false, scheduler)
		proceedUntilQueueIsEmpty()
		input.model.toggleBit(0, false, scheduler)
		proceedUntilQueueIsEmpty()
	}
}
