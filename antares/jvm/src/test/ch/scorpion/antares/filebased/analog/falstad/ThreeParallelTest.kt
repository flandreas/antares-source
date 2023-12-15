package ch.scorpion.antares.filebased.analog.falstad

import ch.scorpion.antares.filebased.analog.kirchhoff.AbstractAnalogFileBasedTest
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class ThreeParallelTest : AbstractAnalogFileBasedTest() {

	private lateinit var ev1: AnalogEdgeView
	private lateinit var ev2: AnalogEdgeView
	private lateinit var ev3: AnalogEdgeView
	private lateinit var ev4: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("4d278669-76be-4e1f-bca4-94a871b5c329"))
		ev1 = openedCircuitView.getWithId(5) as AnalogEdgeView
		ev2 = openedCircuitView.getWithId(7) as AnalogEdgeView
		ev3 = openedCircuitView.getWithId(10) as AnalogEdgeView
		ev4 = openedCircuitView.getWithId(11) as AnalogEdgeView
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertNoIssues()
		assertCurrent(0.15, ev1.current)
		assertCurrent(0.05, ev2.current)
		assertCurrent(0.05, ev3.current)
		assertCurrent(0.05, ev4.current)
	}
}