package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit.*
import kotlin.test.Test

class NotCalculatorTest : AbstractGateCalculatorTest(NotCalculator()) {

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertOneInput(False, True)
		assertOneInput(True, False)
		assertOneInput(Undefined, False)
		assertOneInput(Error, Error)
	}
}
