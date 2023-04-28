package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SingleCurrentSourceTest : AbstractAnalogFileBasedTest() {

	private lateinit var leftEdgeView: AnalogEdgeView
	private lateinit var rightEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("11d5650a-3f3d-4110-aee3-4055e3b5ce51"))
		leftEdgeView = openedCircuitView.getWithId(6) as AnalogEdgeView
		rightEdgeView = openedCircuitView.getWithId(7) as AnalogEdgeView
		startSimulation()
	}

	@Test
	fun shouldSimulate() {
		processUntilQueueIsEmpty()
		assertEquals(0.1, leftEdgeView.current)
		assertEquals(0.1, rightEdgeView.current)
		assertEquals(10.0, leftEdgeView.model.signal!!.voltage)
	}
}