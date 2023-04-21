package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.ResistorView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TransistorPTypeTest : AbstractAnalogFileBasedTest() {

	private lateinit var drainEdgeView: AnalogEdgeView
	private lateinit var variableResistorView: ResistorView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("d0421e9e-dd9b-404d-8b22-b278fdcef535"))
		drainEdgeView = openedCircuitView.getWithId(9) as AnalogEdgeView
		variableResistorView = openedCircuitView.getWithId(3) as ResistorView
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		val drainVoltage = drainEdgeView.model.signal!!.voltage

		// Larger resistance -> smaller gate voltage -> smaller drain/source resistance
		// => smaller drain voltage
		variableResistorView.model.setState(2 * variableResistorView.resistance, scheduler, analogGraphView)
		processUntilQueueIsEmpty()

		assertTrue(drainVoltage > drainEdgeView.model.signal!!.voltage)
	}
}