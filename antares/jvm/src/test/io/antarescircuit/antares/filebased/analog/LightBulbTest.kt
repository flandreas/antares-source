package io.antarescircuit.antares.filebased.analog

import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.antares.view.analog.ResistorView
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class LightBulbTest : AbstractAnalogFileBasedTest() {

	private lateinit var resistorView: ResistorView
	private lateinit var edgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("62591f2d-8c5b-40d7-ad0b-b1f9427b5ac8"))
		resistorView = openedCircuitView.getWithId(2) as ResistorView
		edgeView = openedCircuitView.getWithId(5) as AnalogEdgeView
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertNoIssues()
		assertCurrent(0.041, edgeView.current)
		assertVoltage(0.83, edgeView.getNodeVoltage(0))
	}
}