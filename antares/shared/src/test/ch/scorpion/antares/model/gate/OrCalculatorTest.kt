package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import kotlin.test.Test

/** Unit tests for [OrCalculator]. */
class OrCalculatorTest : AbstractGateCalculatorTest(OrCalculator()) {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFulfillTruthTable() {
		assertTwoInput(False, False, False)
		assertTwoInput(False, True, True)
		assertTwoInput(False, Undefined, Error)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, True)
		assertTwoInput(True, True, True)
		assertTwoInput(True, Undefined, True)
		assertTwoInput(True, Error, True)

		assertTwoInput(Undefined, False, Error)
		assertTwoInput(Undefined, True, True)
		assertTwoInput(Undefined, Undefined, Error)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, True)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}
}