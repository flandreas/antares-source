package ch.scorpion.antares.filebased.analog.kirchhoff

import ch.scorpion.antares.view.analog.AnalogCircuitInOutView
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.near
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransistorPTypeAsInverterTest : AbstractAnalogFileBasedTest() {

	private lateinit var input: AnalogCircuitInOutView
	private lateinit var output: AnalogCircuitInOutView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("64fa06f3-cb8e-4c1b-8d5b-4c28329c32d2"))

		input = openedCircuitView.getWithId(12) as AnalogCircuitInOutView
		output = openedCircuitView.getWithId(6) as AnalogCircuitInOutView
	}

	@Test
	fun shouldInvertOnStartup() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertEquals(0.0, input.model.signal?.voltage)
		assertEquals(5.0, output.model.signal?.voltage)
	}

	@Test
	fun shouldToggleInput() {
		startSimulation()
		processUntilQueueIsEmpty()

		input.model.toggle(scheduler, analogGraphView)
		processUntilQueueIsEmpty()

		assertEquals(5.0, input.model.signal?.voltage)
		assertTrue(output.model.signal?.voltage?.near(2.5, 0.1) == true)
	}
}