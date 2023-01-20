package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.view.analog.*
import ch.scorpion.jabbah.base.UUID
import kotlin.test.*

class KirchhoffTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private val analogGraphView: AnalogGraphView get() = openedCircuitView as AnalogGraphView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("6ffbf463-8095-48f0-bc55-66e920db6103"))
	}

	@Test
	fun shouldIdentifyGroundNode() {
		assertEquals(10, KirchhoffAnalogCircuitCalculator.identifyGroundNode(analogGraphView))
	}

	@Test
	fun shouldLabelVoltageNodes() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 10)

		assertEquals(4, voltageNodes.size)
		assertTrue(voltageNodes.contains(7))
		assertTrue(voltageNodes.contains(8))
		assertTrue(voltageNodes.contains(9))
		assertTrue(voltageNodes.contains(11))
	}

	@Test
	fun shouldIdentifyBranchesRecursively() {
		val batteryView = analogGraphView.getWithId(1) as BatteryView
		val incomingEdgeView = analogGraphView.getWithId(20) as AnalogEdgeView
		val branches = mutableListOf<AnalogCircuitBranch>()

		KirchhoffAnalogCircuitCalculator.identifyBranches(batteryView, incomingEdgeView, analogGraphView, branches)

		assertEquals(4, branches.size)

		assertEquals(2, branches[0].size)
		assertTrue(branches[0].containsValue(7))
		assertTrue(branches[0].containsValue(8))

		assertEquals(3, branches[1].size)
		assertTrue(branches[1].containsValue(13))
		assertTrue(branches[1].containsValue(14))
		assertTrue(branches[1].containsValue(-17))

		assertEquals(2, branches[2].size)
		assertTrue(branches[2].containsValue(16))
		assertTrue(branches[2].containsValue(20))

		assertEquals(3, branches[3].size)
		assertTrue(branches[3].containsValue(-9))
		assertTrue(branches[3].containsValue(-10))
		assertTrue(branches[3].containsValue(-12))
	}

	@Test
	fun shouldLabelBranchCurrents() {
		val branchCurrents = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)

		assertEquals(4, branchCurrents.size)
	}

	@Test
	fun shouldComposeCurrentLawEquations() {
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)

		val equations = KirchhoffAnalogCircuitCalculator.composeCurrentLawEquations(analogGraphView, branches, 10)

		assertEquals(1, equations.size)
		assertEquals(4, equations[0].size)

		assertEquals(1.0, equations[0][0])
		assertEquals(-1.0, equations[0][1])
		assertEquals(0.0, equations[0][2])
		assertEquals(-1.0, equations[0][3])
	}
}