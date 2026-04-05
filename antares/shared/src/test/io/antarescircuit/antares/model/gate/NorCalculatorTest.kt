package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Nor
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals


/** Unit tests for [NorCalculator]. */
class NorCalculatorTest : AbstractGateCalculatorTest(Nor) {

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertTwoInput(Bit.False, Bit.False, Bit.True)
		assertTwoInput(Bit.False, Bit.True, Bit.False)
		assertTwoInput(Bit.False, Bit.Undefined, Bit.True)
		assertTwoInput(Bit.False, Bit.Error, Bit.Error)

		assertTwoInput(Bit.True, Bit.False, Bit.False)
		assertTwoInput(Bit.True, Bit.True, Bit.False)
		assertTwoInput(Bit.True, Bit.Undefined, Bit.False)
		assertTwoInput(Bit.True, Bit.Error, Bit.Error)

		assertTwoInput(Bit.Undefined, Bit.False, Bit.True)
		assertTwoInput(Bit.Undefined, Bit.True, Bit.False)
		assertTwoInput(Bit.Undefined, Bit.Undefined, Bit.True)
		assertTwoInput(Bit.Undefined, Bit.Error, Bit.Error)

		assertTwoInput(Bit.Error, Bit.False, Bit.Error)
		assertTwoInput(Bit.Error, Bit.True, Bit.Error)
		assertTwoInput(Bit.Error, Bit.Undefined, Bit.Error)
		assertTwoInput(Bit.Error, Bit.Error, Bit.Error)
	}

	@Test
	fun shouldCalculateMultiBit() {
		val norGate = NonUnaryLogicGate(Nor, bitWidth = BitWidth.BW_2)
		norGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(Bit.True, Bit.False)), signalHandler)
		norGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(Bit.False, Bit.False)), signalHandler)

		val result = gateType.calculator.calculateMultiBit(norGate)

		assertEquals(Word(listOf(Bit.False, Bit.True)), result)
	}
}
