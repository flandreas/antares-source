package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.view.analog.*
import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator.composeComponentConstituentEquations
import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator.composeCurrentLawEquations
import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator.composeEquations
import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator.labelBranchCurrents
import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator.labelVoltageNodes
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.LinearEquationSystemSolverJvm
import ch.scorpion.jabbah.base.math.near
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.*

class KirchhoffTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			BaseModule.linearEquationSystemSolver = LinearEquationSystemSolverJvm
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
		val voltageNodes = labelVoltageNodes(analogGraphView, 10)

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

		assertEquals(3, branches.size)

		var branch = branches.first { it.containsId(7) }
		assertEquals(4, branch.size)
		assertTrue(branch.containsValue(7))
		assertTrue(branch.containsValue(8))
		assertTrue(branch.containsValue(16))
		assertTrue(branch.containsValue(20))

		branch = branches.first { it.containsId(13) }
		assertEquals(3, branch.size)
		assertTrue(branch.containsValue(13))
		assertTrue(branch.containsValue(14))
		assertTrue(branch.containsValue(-17))

		branch = branches.first { it.containsId(9) }
		assertEquals(3, branch.size)
		assertTrue(branch.containsValue(-9))
		assertTrue(branch.containsValue(-10))
		assertTrue(branch.containsValue(-12))
	}

	@Test
	fun shouldLabelBranchCurrents() {
		val branchCurrents = labelBranchCurrents(analogGraphView)

		// Must merge branches at BatteryView
		assertEquals(3, branchCurrents.size)
	}

	@Test
	fun shouldComposeCurrentLawEquations() {
		val branches = labelBranchCurrents(analogGraphView)
		val equationSystem = DynamicLinearEquationSystem(branches.size)

		composeCurrentLawEquations(analogGraphView, branches, 10, equationSystem)

		// The ground Net is ignored, leaving 1 node where 3 branches meet
		assertEquals(1, equationSystem.equationCount)

		assertEquals(3, equationSystem.getCoefficients(0).size)
		assertEquals(1.0, equationSystem.getCoefficients(0)[0])
		assertEquals(-1.0, equationSystem.getCoefficients(0)[1])
		assertEquals(1.0, equationSystem.getCoefficients(0)[2])
	}

	@Test
	fun shouldComposeComponentConstituentEquations() {
		val voltageNodes = labelVoltageNodes(analogGraphView, 10)
		val branches = labelBranchCurrents(analogGraphView)
		val equationSystem = DynamicLinearEquationSystem(4 + 6)

		composeComponentConstituentEquations(analogGraphView, voltageNodes, branches, equationSystem)

		// 1 Battery, 1 Switch, 2 LightBulbs, 2 Resistors
		assertEquals(6, equationSystem.equationCount)
	}

	// Coefficients determined by manually composing equation system
	@Test
	fun shouldComposeEquationsSwitchedOff() {
		val voltageNodes = labelVoltageNodes(analogGraphView, 10)
		val branches = labelBranchCurrents(analogGraphView)
		val equationSystem = DynamicLinearEquationSystem(1 + 6)

		composeEquations(analogGraphView, voltageNodes, branches, 10, equationSystem)

		assertEquals(7, equationSystem.equationCount)

		assertTrue(arrayOf(1.0, -1.0, 1.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray() contentEquals equationSystem.getCoefficients(0))
		assertTrue(arrayOf(0.0, -500.0, 0.0, 0.0, 0.0, 0.0, 1.0).toDoubleArray() contentEquals equationSystem.getCoefficients(1))
		assertTrue(arrayOf(0.0, -20.0, 0.0, 0.0, 1.0, 0.0, -1.0).toDoubleArray() contentEquals equationSystem.getCoefficients(2))
		assertTrue(arrayOf(0.0, 0.0, -20.0, 0.0, -1.0, 1.0, 0.0).toDoubleArray() contentEquals equationSystem.getCoefficients(3))
		assertTrue(arrayOf(0.0, 0.0, -100.0, 0.0, 0.0, -1.0, 0.0).toDoubleArray() contentEquals equationSystem.getCoefficients(4))
		assertTrue(arrayOf(-100_000_000.0, 0.0, 0.0, 1.0, -1.0, 0.0, 0.0).toDoubleArray() contentEquals equationSystem.getCoefficients(5))
		assertTrue(arrayOf(0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0).toDoubleArray() contentEquals equationSystem.getCoefficients(6))

		assertTrue(arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -5.0).toDoubleArray() contentEquals equationSystem.getConstants().toTypedArray().toDoubleArray())
	}

	@Test
	fun shouldSolveSwitchedOn() {
		(analogGraphView.getWithId(2) as AnalogSwitchView).model.toggle(scheduler, analogGraphView)

		val voltageNodes = labelVoltageNodes(analogGraphView, 10)
		val branches = labelBranchCurrents(analogGraphView)
		val equationSystem = DynamicLinearEquationSystem(1 + 6)

		composeEquations(analogGraphView, voltageNodes, branches, 10, equationSystem)
		val result = BaseModule.linearEquationSystemSolver.solve(equationSystem.toLinearEquationSystem())

		assertTrue(result[0].near(0.05, 0.01))
		assertTrue(result[1].near(0.009, 0.001))
		assertTrue(result[2].near(-0.04, 0.01))
		assertTrue(result[3].near(5.0, 0.01))
		assertTrue(result[4].near(5.0, 0.01))
		assertTrue(result[5].near(4.1, 0.1))
		assertTrue(result[6].near(4.8, 0.1))
	}

	@Test
	fun shouldCalculateResultSwitchedOff() {
		val analysis = KirchhoffAnalogCircuitCalculator.analyse(analogGraphView)
		KirchhoffAnalogCircuitCalculator.calculate(analysis, scheduler)

		// Voltages
		assertEquals(5.0, (analogGraphView.graph!!.withId(7) as AnalogNet).signal!!.voltage)
		assertTrue((analogGraphView.graph!!.withId(8) as AnalogNet).signal!!.voltage <= 0.00001)
		assertTrue((analogGraphView.graph!!.withId(9) as AnalogNet).signal!!.voltage <= 0.00001)
		assertTrue((analogGraphView.graph!!.withId(10) as AnalogNet).signal!!.voltage <= 0.00001)
		assertTrue((analogGraphView.graph!!.withId(11) as AnalogNet).signal!!.voltage <= 0.00001)

		// Currents
		assertTrue(analogGraphView.getDrawables { it is AnalogEdgeView }.map { it as AnalogEdgeView }.all { it.current <= 0.00001 })
	}

	@Test
	fun shouldCalculateResultSwitchedOn() {
		(analogGraphView.getWithId(2) as AnalogSwitchView).model.toggle(scheduler, analogGraphView)

		val analysis = KirchhoffAnalogCircuitCalculator.analyse(analogGraphView)
		KirchhoffAnalogCircuitCalculator.calculate(analysis, scheduler)

		// Voltages
		assertEquals(5.0, (analogGraphView.graph!!.withId(7) as AnalogNet).signal!!.voltage)
		assertEquals(5.0, (analogGraphView.graph!!.withId(8) as AnalogNet).signal!!.voltage)
		assertTrue((analogGraphView.graph!!.withId(9) as AnalogNet).signal!!.voltage.near(4.16, 0.01))
		assertTrue((analogGraphView.graph!!.withId(11) as AnalogNet).signal!!.voltage.near(4.8, 0.1))
		assertTrue((analogGraphView.graph!!.withId(10) as AnalogNet).signal!!.voltage.near(0.0, 0.01))

		// Currents
		assertTrue(getAnalogEdgeView(analogGraphView, 7).current.near(0.05, 0.01))
		assertTrue(getAnalogEdgeView(analogGraphView, 8).current.near(0.05, 0.01))

		assertTrue(getAnalogEdgeView(analogGraphView, 9).current.near(0.04, 0.01))
		assertTrue(getAnalogEdgeView(analogGraphView, 10).current.near(0.04, 0.01))
		assertTrue(getAnalogEdgeView(analogGraphView, 12).current.near(0.04, 0.01))

		assertTrue(getAnalogEdgeView(analogGraphView, 13).current.near(0.009, 0.001))
		assertTrue(getAnalogEdgeView(analogGraphView, 14).current.near(0.009, 0.001))
		assertTrue(getAnalogEdgeView(analogGraphView, 17).current.near(-0.009, 0.001))

		assertTrue(getAnalogEdgeView(analogGraphView, 16).current.near(0.05, 0.01))
		assertTrue(getAnalogEdgeView(analogGraphView, 20).current.near(0.05, 0.01))

		assertTrue(getAnalogEdgeView(analogGraphView, 21).current.near(0.00, 0.01))
	}

	private fun getAnalogEdgeView(graphView: GraphView, id: Int): AnalogEdgeView =
		graphView.getEdgeViews().map { it as AnalogEdgeView }.first { it.id == id }
}