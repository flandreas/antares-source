package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import kotlin.test.Test

/** Unit tests for [AndCalculator]. */
class AndCalculatorTest : AbstractGateCalculatorTest(AndCalculator()){

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

	@Test
	fun shouldFulfillTruthTable() {
		assertTwoInput(False, False, False)
		assertTwoInput(False, True, False)
		assertTwoInput(False, Undefined, False)
		assertTwoInput(False, Error, False)

		assertTwoInput(True, False, False)
		assertTwoInput(True, True, True)
		assertTwoInput(True, Undefined, Error)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, False)
		assertTwoInput(Undefined, True, Error)
		assertTwoInput(Undefined, Undefined, Error)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, False)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}
}
