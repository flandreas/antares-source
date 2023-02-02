package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.view.analog.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.LinearEquationSystemSolverJvm
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransistorTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			BaseModule.linearEquationSystemSolver = LinearEquationSystemSolverJvm
		}
	}

	private val analogGraphView: AnalogGraphView get() = openedCircuitView as AnalogGraphView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("39825aea-1002-424c-b918-05173d07d3f0"))
	}

	@Test
	fun shouldLabelVoltageNodes() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 6)
		assertEquals(3, voltageNodes.size)
	}

	@Test
	fun shouldIdentifyBranches() {
		val batteryView = analogGraphView.getWithId(4) as BatteryView
		val incomingEdgeView = analogGraphView.getWithId(11) as AnalogEdgeView
		val branches = mutableListOf<AnalogCircuitBranch>()

		KirchhoffAnalogCircuitCalculator.identifyBranches(batteryView, incomingEdgeView, analogGraphView, branches)

		assertEquals(5, branches.size)

		var branch = branches.first { it.containsId(5) }
		assertTrue(branch.containsId(5))

		branch = branches.first { it.containsId(8) }
		assertTrue(branch.containsId(8))
		assertTrue(branch.containsId(9))
		assertTrue(branch.containsId(17))

		branch = branches.first { it.containsId(14) }
		assertTrue(branch.containsId(14))

		branch = branches.first { it.containsId(7) }
		assertTrue(branch.containsId(7))
		assertTrue(branch.containsId(10))

		branch = branches.first { it.containsId(15) }
		assertTrue(branch.containsId(15))
		assertTrue(branch.containsId(18))
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
	}
}