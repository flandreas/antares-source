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

class OhmsLawTest : AbstractAnalogFileBasedTest() {

	private lateinit var powerEdgeView: AnalogEdgeView
	private lateinit var ground100EdgeView: AnalogEdgeView
	private lateinit var ground900EdgeView: AnalogEdgeView

	@BeforeTest
	fun openCircuit() {
		AntaresViewModule.analogCircuitCalculatorFactor = object : AnalogCircuitCalculatorFactory {
			override fun <T : AnalogCircuitAnalysis> create(): AnalogCircuitCalculator<T> {
				return FalstadAnalogCircuitCalculator() as AnalogCircuitCalculator<T>
			}
		}
		openCircuit(UUID("e0d5ab6a-4476-47b9-9200-0948c692a16a"))

		powerEdgeView = openedCircuitView.getWithId(6) as AnalogEdgeView
		ground100EdgeView = openedCircuitView.getWithId(10) as AnalogEdgeView
		ground900EdgeView = openedCircuitView.getWithId(11) as AnalogEdgeView
	}

	@Test
	fun shouldAnalyse() {
		FalstadAnalogCircuitAnalyzer(analogGraphView).analyse()
	}

	@Test
	fun shouldCalculate() {
		startSimulation()
		processUntilQueueIsEmpty()

		assertNoIssues()

		assertVoltage(5.0, powerEdgeView.getNodeVoltage(0))
		assertVoltage(0.0, ground100EdgeView.getNodeVoltage(0))
		assertVoltage(0.0, ground900EdgeView.getNodeVoltage(0))

		assertCurrent(0.055, powerEdgeView.current)
		assertCurrent(0.050, ground100EdgeView.current)
		assertCurrent(0.005, ground900EdgeView.current)
	}
}