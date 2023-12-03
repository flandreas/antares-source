package ch.scorpion.antares.filebased.analog.falstad

import ch.scorpion.antares.filebased.analog.kirchhoff.AbstractAnalogFileBasedTest
import ch.scorpion.antares.view.analog.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.AnalogCircuitCalculator
import ch.scorpion.antares.view.analog.AnalogCircuitCalculatorFactory
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalyzer
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitCalculator
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class OhmsLawTest : AbstractAnalogFileBasedTest() {

	@BeforeTest
	fun openCircuit() {
		AntaresViewModule.analogCircuitCalculatorFactor = object : AnalogCircuitCalculatorFactory {
			override fun <T : AnalogCircuitAnalysis> create(): AnalogCircuitCalculator<T> {
				return FalstadAnalogCircuitCalculator() as AnalogCircuitCalculator<T>
			}
		}
		openCircuit(UUID("e0d5ab6a-4476-47b9-9200-0948c692a16a"))
	}

	@Test
	fun shouldAnalyse() {
		FalstadAnalogCircuitAnalyzer(analogGraphView).analyse()
	}
}