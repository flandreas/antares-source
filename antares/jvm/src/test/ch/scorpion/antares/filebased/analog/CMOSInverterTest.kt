package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.KirchhoffAnalogCircuitCalculator
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CMOSInverterTest : AbstractAnalogFileBasedTest() {

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("85e1c5af-7c78-485c-959a-6d0967353799"))
	}

	@Test
	fun shouldAnalyze() {
		val analysis = KirchhoffAnalogCircuitCalculator.analyse(analogGraphView)

		assertEquals(3, analysis.voltageNodeNetIds.size)
		assertEquals(6, analysis.branches.size)
		assertTrue(analysis.equationSystem.toLinearEquationSystem().isNonSingular, "System is singular")
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()
		assertNoIssues()
	}
}