package ch.scorpion.antares.filebased.analog

import ch.scorpion.antares.filebased.AbstractFileBasedTest
import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.math.LinearEquationSystemSolverJvm
import ch.scorpion.jabbah.base.module.BaseModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SerialBatteriesTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
			BaseModule.linearEquationSystemSolver = LinearEquationSystemSolverJvm
		}
	}

	private val analogGraphView: AnalogGraphView get() = openedCircuitView as AnalogGraphView

	@BeforeTest
	fun openCircuit() {
		openCircuit(UUID("3566b7b9-5949-46d6-84e0-1853d6c4314a"))
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		val net = analogGraphView.graph!!.withId(5) as AnalogNet
		assertEquals(10.0, net.signal!!.voltage)
	}
}