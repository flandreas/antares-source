package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.signal.Bit.*
import kotlin.test.Test

class NotCalculatorTest : AbstractGateCalculatorTest(UnaryLogicGateType.Not) {

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertOneInput(False, True)
		assertOneInput(True, False)
		assertOneInput(Undefined, False)
		assertOneInput(Error, Error)
	}
}
