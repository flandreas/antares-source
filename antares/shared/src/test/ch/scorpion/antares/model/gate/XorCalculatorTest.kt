package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import kotlin.test.Test

/** Unit tests for [XorCalculator]. */
class XorCalculatorTest : AbstractGateCalculatorTest(XorCalculator()){

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
		assertTwoInput(True, True, False)
		assertTwoInput(True, Undefined, Error)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, Error)
		assertTwoInput(Undefined, True, Error)
		assertTwoInput(Undefined, Undefined, Error)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}
}
