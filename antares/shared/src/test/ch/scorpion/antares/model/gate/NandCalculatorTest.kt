package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import kotlin.test.Test

/** Unit tests for [AndCalculator]. */
class NandCalculatorTest : AbstractGateCalculatorTest(NandCalculator()) {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFulfillTruthTable() {
		assertTwoInput(False, False, True)
		assertTwoInput(False, True, True)
		assertTwoInput(False, Undefined, True)
		assertTwoInput(False, Error, True)

		assertTwoInput(True, False, True)
		assertTwoInput(True, True, False)
		assertTwoInput(True, Undefined, Error)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, True)
		assertTwoInput(Undefined, True, Error)
		assertTwoInput(Undefined, Undefined, Error)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, True)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}
}