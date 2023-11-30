package ch.scorpion.antares.filebased.analog.kirchhoff

import ch.scorpion.antares.view.analog.*
import ch.scorpion.antares.view.analog.kirchhoff.KirchhoffAnalogCircuitCalculator
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThreeParallelTest : AbstractAnalogFileBasedTest() {

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("4d278669-76be-4e1f-bca4-94a871b5c329"))
	}

	@Test
	fun shouldLabelVoltageNodes() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 6)
		assertEquals(1, voltageNodes.size)
	}

	@Test
	fun shouldIdentifyBranches() {
		val batteryView = analogGraphView.getWithId(1) as BatteryView
		val incomingEdgeView = analogGraphView.getWithId(14) as AnalogEdgeView
		val branches = mutableListOf<AnalogCircuitBranch>()

		KirchhoffAnalogCircuitCalculator.identifyBranches(batteryView, incomingEdgeView, analogGraphView, branches)

		assertEquals(6, branches.size)
	}

	@Test
	fun shouldComposeCurrentLawEquations() {
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)
		val equationSystem = DynamicLinearEquationSystem(branches.size)

		KirchhoffAnalogCircuitCalculator.composeCurrentLawEquations(analogGraphView, branches, equationSystem)

		// The linearly dependent second equation will be removed when composing ALL equations
		assertEquals(4, equationSystem.equationCount)
	}

	@Test
	fun shouldComposeComponentConstituentEquations() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 6)
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)
		val equationSystem = DynamicLinearEquationSystem(6 + 1)

		KirchhoffAnalogCircuitCalculator.composeComponentConstituentEquations(analogGraphView, voltageNodes, branches, 6, equationSystem)

		assertEquals(4, equationSystem.equationCount)
	}

	@Test
	fun shouldComposeEquations() {
		val voltageNodes = KirchhoffAnalogCircuitCalculator.labelVoltageNodes(analogGraphView, 6)
		val branches = KirchhoffAnalogCircuitCalculator.labelBranchCurrents(analogGraphView)
		val equationSystem = DynamicLinearEquationSystem(6 + 1)

		KirchhoffAnalogCircuitCalculator.composeEquations(analogGraphView, voltageNodes, branches, 6, equationSystem)

		assertEquals(7, equationSystem.equationCount)
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
	}
}