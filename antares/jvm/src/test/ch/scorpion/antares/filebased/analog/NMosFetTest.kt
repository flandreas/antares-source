package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogCircuitInOutView
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class NMosFetTest : AbstractAnalogFileBasedTest() {

	private lateinit var inputView: AnalogCircuitInOutView
	private lateinit var powerEdgeView: AnalogEdgeView
	private lateinit var groundEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("3a94790e-644d-409b-812d-08aa8fee206e"))

		inputView = openedCircuitView.getWithId(1) as AnalogCircuitInOutView
		powerEdgeView = openedCircuitView.getWithId(6) as AnalogEdgeView
		groundEdgeView = openedCircuitView.getWithId(7) as AnalogEdgeView
	}

	@Test
	fun shouldCalculateWithLowInput() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertNoIssues()

		assertCurrent(0.0, -powerEdgeView.current)
		assertCurrent(0.0, groundEdgeView.current)
	}

	@Test
	fun shouldCalculateWithHighInput() {
		startSimulation()
		processUntilQueueIsEmpty()

		inputView.model.toggle(scheduler, openedCircuitView)
		processUntilQueueIsEmpty()

		assertNoIssues()

		assertCurrent(0.122, -powerEdgeView.current)
		assertCurrent(0.122, groundEdgeView.current)
	}
}