package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PowerTest : AbstractAnalogFileBasedTest() {

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("f113a3bb-4603-49b6-a2ad-1eccb5c84c6c"))
	}

	@Test
	fun shouldLabelVoltageNodes() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 10)
		assertEquals(3, voltageNodes.size)
	}

	@Test
	fun shouldLabelBranchCurrents() {
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)

		assertEquals(1, branches.size)

		with (branches[0]) {
			assertEquals(4, size)
			assertTrue(containsId(5))
			assertTrue(containsId(6))
			assertTrue(containsId(8))
			assertTrue(containsId(10))
		}
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
	}
}