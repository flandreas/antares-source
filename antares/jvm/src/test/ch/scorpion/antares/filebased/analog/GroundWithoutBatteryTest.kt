package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroundWithoutBatteryTest : AbstractAnalogFileBasedTest() {

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("e046b285-8c56-40ea-8696-88015af7a89a"))
	}

	@Test
	fun shouldIdentifyGroundNode() {
		assertEquals(8, KirchhoffAnalogCircuitCalculator.identifyGroundNode(analogGraphView))
	}

	@Test
	fun shouldLabelVoltageNodes() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 8)

		assertEquals(2, voltageNodes.size)
		assertTrue(voltageNodes.contains(5))
		assertTrue(voltageNodes.contains(6))
	}

	@Test
	fun shouldLabelBranchCurrents() {
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)

		assertEquals(3, branches.size)

		var branch = branches.first { it.containsId(5) }
		assertEquals(2, branch.size)
		assertTrue(branch.containsId(5))
		assertTrue(branch.containsId(8))
0
		branch = branches.first { it.containsId(6) }
		assertEquals(2, branch.size)
		assertTrue(branch.containsId(6))
		assertTrue(branch.containsId(11))

		branch = branches.first { it.containsId(10) }
		assertEquals(1, branch.size)
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
	}
}