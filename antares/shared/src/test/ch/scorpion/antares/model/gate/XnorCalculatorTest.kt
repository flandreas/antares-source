package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import kotlin.test.Test

/** Unit tests for [XnorCalculator]. */
class XnorCalculatorTest : AbstractGateCalculatorTest(XnorCalculator()){

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFulfillTruthTable() {
		assertTwoInput(False, False, True)
		assertTwoInput(False, True, False)
		assertTwoInput(False, Undefined, True)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, False)
		assertTwoInput(True, True, True)
		assertTwoInput(True, Undefined, True)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, True)
		assertTwoInput(Undefined, True, True)
		assertTwoInput(Undefined, Undefined, True)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}
}
