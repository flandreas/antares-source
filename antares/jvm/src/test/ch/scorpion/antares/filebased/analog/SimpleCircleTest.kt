package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.view.analog.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.LinearEquationSystemSolverJvm
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleCircleTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			BaseModule.linearEquationSystemSolver = LinearEquationSystemSolverJvm
		}
	}

	private val analogGraphView: AnalogGraphView get() = openedCircuitView as AnalogGraphView

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