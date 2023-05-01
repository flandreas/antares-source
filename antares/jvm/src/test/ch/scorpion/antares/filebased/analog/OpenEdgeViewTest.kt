package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.BatteryView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenEdgeViewTest : AbstractAnalogFileBasedTest() {

	private lateinit var batteryView: BatteryView
	private lateinit var batteryEdgeView: AnalogEdgeView
	private lateinit var openEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("b3c417d1-f1d9-46ff-a41b-aab6467c49a4"))
		batteryView = analogGraphView.getWithId(1) as BatteryView
		batteryEdgeView = analogGraphView.getWithId(6) as AnalogEdgeView
		openEdgeView = analogGraphView.getWithId(7) as AnalogEdgeView
	}

	@Test
	fun shouldSimulate() {
		startSimulation()

		processUntilQueueIsEmpty()

		assertNoIssues()
		assertEquals(0.0, openEdgeView.current)
		assertEquals(0.05, batteryEdgeView.current)
	}
}