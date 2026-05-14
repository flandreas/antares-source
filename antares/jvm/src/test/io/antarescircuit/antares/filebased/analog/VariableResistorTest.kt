package io.antarescircuit.antares.filebased.analog

import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.AnalogGraphView
import io.antarescircuit.antares.view.analog.ResistorView
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalyzer
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
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

		resistorView.model.setState(MagnitudeValue(200.0, Magnitude.One, SIUnit.Ohm), scheduler, openedCircuitView as AnalogGraphView)
		processUntilQueueIsEmpty()

		assertCurrent(0.025, posEdgeView.current)
		assertCurrent(0.025, negEdgeView.current)

	}
}