package ch.scorpion.antares.filebased.analog.falstad

import ch.scorpion.antares.filebased.analog.kirchhoff.AbstractAnalogFileBasedTest
import ch.scorpion.antares.view.analog.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.AnalogCircuitCalculator
import ch.scorpion.antares.view.analog.AnalogCircuitCalculatorFactory
import ch.scorpion.antares.view.analog.AnalogEdgeView
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalyzer
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitCalculator
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class ResistorTest : AbstractAnalogFileBasedTest() {

	private lateinit var posEdgeView: AnalogEdgeView
	private lateinit var negEdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		AntaresViewModule.analogCircuitCalculatorFactor = object : AnalogCircuitCalculatorFactory {
			override fun <T : AnalogCircuitAnalysis> create(): AnalogCircuitCalculator<T> {
				return FalstadAnalogCircuitCalculator() as AnalogCircuitCalculator<T>
			}
		}
		openCircuit(UUID("608e8835-d7bf-4601-bb69-42f4fc8c64e7"))

		posEdgeView = openedCircuitView.getWithId(3) as AnalogEdgeView
		negEdgeView = openedCircuitView.getWithId(4) as AnalogEdgeView
	}

	@Test
	fun shouldAnalyse() {
		FalstadAnalogCircuitAnalyzer(analogGraphView).analyse()
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertCurrent(0.05, posEdgeView.current)
		assertCurrent(0.05, negEdgeView.current)

		assertVoltage(5.0, posEdgeView.getNodeVoltage(0))
		assertVoltage(0.0, negEdgeView.getNodeVoltage(0))
	}
}