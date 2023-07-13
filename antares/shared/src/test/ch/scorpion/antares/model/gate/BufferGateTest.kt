package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.signal.Bit
import kotlin.test.Test

class BufferCalculatorTest : AbstractGateCalculatorTest(UnaryLogicGateType.Buffer) {

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertOneInput(Bit.False, Bit.False)
		assertOneInput(Bit.True, Bit.True)
		assertOneInput(Bit.Undefined, Bit.False)
		assertOneInput(Bit.Error, Bit.Error)
	}
}
