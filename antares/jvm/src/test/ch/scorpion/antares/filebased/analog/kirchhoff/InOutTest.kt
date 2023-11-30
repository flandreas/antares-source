package ch.scorpion.antares.filebased.analog.kirchhoff

import ch.scorpion.antares.view.analog.kirchhoff.KirchhoffAnalogCircuitCalculator
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class InOutTest : AbstractAnalogFileBasedTest() {

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