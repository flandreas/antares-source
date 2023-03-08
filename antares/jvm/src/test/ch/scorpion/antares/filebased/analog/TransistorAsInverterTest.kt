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

class TransistorAsInverterTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			BaseModule.linearEquationSystemSolver = LinearEquationSystemSolverJvm
		}
	}

	private val analogGraphView: AnalogGraphView get() = openedCircuitView as AnalogGraphView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("af4ddc6f-3edb-434c-8884-0d3378376cb7"))
	}

	@Test
	fun shouldLabelVoltageNodes() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 2)
		assertEquals(4, voltageNodes.size)
	}

	@Test
	fun shouldLabelBranchCurrents() {
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)

		assertEquals(7, branches.size)

		with (branches.first { it.containsId(14) }) {
			assertEquals(2, size)
			assertTrue(containsId(17))
		}
		with (branches.first { it.containsId(24) }) {
			assertEquals(2, size)
			assertTrue(containsId(27))
		}
		with (branches.first { it.containsId(25) }) {
			assertEquals(1, size)
		}
		with (branches.first { it.containsId(32) }) {
			assertEquals(2, size)
			assertTrue(containsId(31))
		}
		with (branches.first { it.containsId(21) }) {
			assertEquals(1, size)
		}
		with (branches.first { it.containsId(36) }) {
			assertEquals(1, size)
		}
		with (branches.first { it.containsId(35) }) {
			assertEquals(2, size)
			assertTrue(containsId(28))
		}
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
	}
}