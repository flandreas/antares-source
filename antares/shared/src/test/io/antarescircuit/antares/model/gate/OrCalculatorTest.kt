package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Or
import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [OrCalculator]. */
class OrCalculatorTest : AbstractGateCalculatorTest(Or) {

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertTwoInput(False, False, False)
		assertTwoInput(False, True, True)
		assertTwoInput(False, Undefined, False)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, True)
		assertTwoInput(True, True, True)
		assertTwoInput(True, Undefined, True)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, False)
		assertTwoInput(Undefined, True, True)
		assertTwoInput(Undefined, Undefined, False)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}

	@Test
	fun shouldCalculateMultiBit() {
		val orGate = NonUnaryLogicGate(Or, bitWidth = BitWidth.BW_2)
		orGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(True, False)), signalHandler)
		orGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(False, False)), signalHandler)

		val result = gateType.calculator.calculateMultiBit(orGate)

		assertEquals(Word(listOf(True, False)), result)
	}
}