package io.antarescircuit.antares.filebased.analog

import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalyzer
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class ResistorTest : AbstractAnalogFileBasedTest() {

	private lateinit var posEdgeView: AnalogEdgeView
	private lateinit var negEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("608e8835-d7bf-4601-bb69-42f4fc8c64e7"))

		posEdgeView = openedCircuitView.getWithId(3) as AnalogEdgeView
		negEdgeView = openedCircuitView.getWithId(4) as AnalogEdgeView
	}

	@Test
	fun shouldAnalyse() {
		AnalogCircuitAnalyzer(analogGraphView).analyse()
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertCurrent(0.05, posEdgeView.current)
		assertCurrent(0.05, negEdgeView.current)

		assertVoltage(5.0, posEdgeView.getNodeVoltage(0))
		assertVoltage(0.0, negEdgeView.getNodeVoltage(0))
	}
}