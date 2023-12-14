package ch.scorpion.antares.filebased.analog.falstad

import ch.scorpion.antares.filebased.analog.kirchhoff.AbstractAnalogFileBasedTest
import ch.scorpion.antares.view.analog.*
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalyzer
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitCalculator
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class VariableResistorTest : AbstractAnalogFileBasedTest() {

	private lateinit var resistorView: ResistorView
	private lateinit var posEdgeView: AnalogEdgeView
	private lateinit var negEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		AntaresViewModule.analogCircuitCalculatorFactor = object : AnalogCircuitCalculatorFactory {
			override fun <T : AnalogCircuitAnalysis> create(): AnalogCircuitCalculator<T> {
				return FalstadAnalogCircuitCalculator() as AnalogCircuitCalculator<T>
			}
		}
		openCircuit(UUID("f8af7b2b-0185-4e7d-8eed-18fa3130d6ea"))

		resistorView = openedCircuitView.getWithId(2) as ResistorView
		posEdgeView = openedCircuitView.getWithId(3) as AnalogEdgeView
		negEdgeView = openedCircuitView.getWithId(4) as AnalogEdgeView
	}

	@Test
	fun shouldAnalyse() {
		FalstadAnalogCircuitAnalyzer(analogGraphView).analyse()
	}

	@Test
	fun shouldChangeResistance() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertCurrent(0.05, posEdgeView.current)
		assertCurrent(0.05, negEdgeView.current)
		assertVoltage(5.0, posEdgeView.getNodeVoltage(0))
		assertVoltage(0.0, negEdgeView.getNodeVoltage(0))

		resistorView.model.setState(200.0, scheduler, openedCircuitView as AnalogGraphView)
		processUntilQueueIsEmpty()

		assertCurrent(0.025, posEdgeView.current)
		assertCurrent(0.025, negEdgeView.current)

	}
}