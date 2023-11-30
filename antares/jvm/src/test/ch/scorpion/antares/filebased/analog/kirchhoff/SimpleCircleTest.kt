package ch.scorpion.antares.filebased.analog.kirchhoff

import ch.scorpion.antares.view.analog.AnalogCircuitBranch
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.BatteryView
import ch.scorpion.antares.view.analog.kirchhoff.KirchhoffAnalogCircuitCalculator
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleCircleTest : AbstractAnalogFileBasedTest() {

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("8221d057-4f62-4fa1-aecd-21cf62bd31f5"))
	}

	@Test
	fun shouldIdentifyBranch() {
		val batteryView = analogGraphView.getWithId(1) as BatteryView
		val incomingEdgeView = analogGraphView.getWithId(6) as AnalogEdgeView
		val branches = mutableListOf<AnalogCircuitBranch>()

		KirchhoffAnalogCircuitCalculator.identifyBranches(batteryView, incomingEdgeView, analogGraphView, branches)

		assertEquals(1, branches.size)
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
	}
}