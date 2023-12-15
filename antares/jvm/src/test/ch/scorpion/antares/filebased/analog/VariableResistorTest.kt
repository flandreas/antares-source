package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.ResistorView
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalyzer
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class VariableResistorTest : AbstractAnalogFileBasedTest() {

	private lateinit var resistorView: ResistorView
	private lateinit var posEdgeView: AnalogEdgeView
	private lateinit var negEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("f8af7b2b-0185-4e7d-8eed-18fa3130d6ea"))

		resistorView = openedCircuitView.getWithId(2) as ResistorView
		posEdgeView = openedCircuitView.getWithId(3) as AnalogEdgeView
		negEdgeView = openedCircuitView.getWithId(4) as AnalogEdgeView
	}

	@Test
	fun shouldAnalyse() {
		AnalogCircuitAnalyzer(analogGraphView).analyse()
	}

	@Test
	fun shouldChangeResistance() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertCurrent(0.05, posEdgeView.current)
		assertCurrent(0.05, negEdgeView.current)
		assertVoltage(5.0, posEdgeView.getNodeVoltage(0))
		assertVoltage(0.0, negEdgeView.getNodeVoltage(0))

		resistorView.model.setState(200.0, scheduler, openedCircuitView as AnalogGraphView)
		processUntilQueueIsEmpty()

		assertCurrent(0.025, posEdgeView.current)
		assertCurrent(0.025, negEdgeView.current)

	}
}