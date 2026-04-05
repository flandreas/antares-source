package io.antarescircuit.antares.filebased.analog

import io.antarescircuit.antares.view.analog.AnalogEdgeView
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class SingleCurrentSourceTest : AbstractAnalogFileBasedTest() {

	private lateinit var leftEdgeView: AnalogEdgeView
	private lateinit var rightEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("11d5650a-3f3d-4110-aee3-4055e3b5ce51"))
		leftEdgeView = openedCircuitView.getWithId(6) as AnalogEdgeView
		rightEdgeView = openedCircuitView.getWithId(7) as AnalogEdgeView
	}

	@Test
	fun shouldSimulate() {
		startSimulation()
		processUntilQueueIsEmpty()
		assertCurrent(0.1, leftEdgeView.current)
		assertCurrent(0.1, rightEdgeView.current)
		assertVoltage(10.0, leftEdgeView.model.signal!!.voltage)
	}
}