package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import kotlin.test.Test


/** Unit tests for [NorCalculator]. */
class NorCalculatorTest : AbstractGateCalculatorTest(NorCalculator()) {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFulfillTruthTable() {
		assertTwoInput(Bit.False, Bit.False, Bit.True)
		assertTwoInput(Bit.False, Bit.True, Bit.False)
		assertTwoInput(Bit.False, Bit.Undefined, Bit.True)
		assertTwoInput(Bit.False, Bit.Error, Bit.Error)

		assertTwoInput(Bit.True, Bit.False, Bit.False)
		assertTwoInput(Bit.True, Bit.True, Bit.False)
		assertTwoInput(Bit.True, Bit.Undefined, Bit.False)
		assertTwoInput(Bit.True, Bit.Error, Bit.False)

		assertTwoInput(Bit.Undefined, Bit.False, Bit.True)
		assertTwoInput(Bit.Undefined, Bit.True, Bit.False)
		assertTwoInput(Bit.Undefined, Bit.Undefined, Bit.True)
		assertTwoInput(Bit.Undefined, Bit.Error, Bit.Error)

		assertTwoInput(Bit.Error, Bit.False, Bit.Error)
		assertTwoInput(Bit.Error, Bit.True, Bit.False)
		assertTwoInput(Bit.Error, Bit.Undefined, Bit.Error)
		assertTwoInput(Bit.Error, Bit.Error, Bit.Error)
	}
}
