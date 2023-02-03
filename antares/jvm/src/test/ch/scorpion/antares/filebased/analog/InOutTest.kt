package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.LinearEquationSystemSolverJvm
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class InOutTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			BaseModule.linearEquationSystemSolver = LinearEquationSystemSolverJvm
		}
	}

	private val analogGraphView: AnalogGraphView get() = openedCircuitView as AnalogGraphView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("34ded684-35ce-4010-9f9a-f5df9db08bf5"))
	}

	@Test
	fun shouldLabelVoltageNodes() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 6)
		assertEquals(2, voltageNodes.size)
	}

	@Test
	fun shouldLabelBranchCurrents() {
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)

		assertEquals(1, branches.size)

		with (branches[0]) {
			assertEquals(3, size)
			assertTrue(containsId(3))
			assertTrue(containsId(6))
			assertTrue(containsId(7))
		}
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
	}
}