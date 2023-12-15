package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.view.analog.AnalogCircuitInOutView
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class InOutTest: AbstractAnalogFileBasedTest() {

	private lateinit var inputView: AnalogCircuitInOutView
	private lateinit var outputView: AnalogCircuitInOutView
	private lateinit var inEdgeView: AnalogEdgeView
	private lateinit var outEdgeView: AnalogEdgeView
	private lateinit var lowerNodeEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("f0986ce8-bdd7-4ed7-be27-61ec0a5d952a"))
		inputView = openedCircuitView.getWithId(1) as AnalogCircuitInOutView
		outputView = openedCircuitView.getWithId(2) as AnalogCircuitInOutView
		inEdgeView = openedCircuitView.getWithId(4) as AnalogEdgeView
		outEdgeView = openedCircuitView.getWithId(9) as AnalogEdgeView
		lowerNodeEdgeView = openedCircuitView.getWithId(10) as AnalogEdgeView
	}

	@Test
	fun shouldCalculateWithLowInput() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertNoIssues()

		assertCurrent(0.0, inEdgeView.current)
		assertCurrent(0.0, outEdgeView.current)
		assertCurrent(0.0, lowerNodeEdgeView.current)

		assertVoltage(0.0, inEdgeView.getNodeVoltage(0))
		assertVoltage(0.0, outEdgeView.getNodeVoltage(0))
		assertVoltage(0.0, lowerNodeEdgeView.getNodeVoltage(0))
	}

	@Test
	fun shouldCalculateWithHighInput() {
		startSimulation()
		processUntilQueueIsEmpty()

		inputView.model.toggle(scheduler, openedCircuitView)
		processUntilQueueIsEmpty()

		assertNoIssues()

		assertCurrent(0.025, inEdgeView.current)
		assertCurrent(0.00, outEdgeView.current)
		assertCurrent(0.025, lowerNodeEdgeView.current)

		assertVoltage(5.0, inEdgeView.getNodeVoltage(0))
		assertVoltage(2.5, outEdgeView.getNodeVoltage(0))
		assertVoltage(2.5, lowerNodeEdgeView.getNodeVoltage(0))
	}
}