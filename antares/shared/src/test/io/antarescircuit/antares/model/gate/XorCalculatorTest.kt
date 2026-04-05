package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Xor
import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [XorCalculator]. */
class XorCalculatorTest : AbstractGateCalculatorTest(Xor){

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertTwoInput(False, False, False)
		assertTwoInput(False, True, True)
		assertTwoInput(False, Undefined, False)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, True)
		assertTwoInput(True, True, False)
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
	fun oddNumberOfInputShouldCalculateTrue() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		val xor = NonUnaryLogicGate(Xor, PortCount.THREE)
		xor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		assertTrue(xor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun evenNumberOfInputShouldCalculateFalse() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		val xor = NonUnaryLogicGate(Xor, PortCount.THREE)
		xor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)

		assertFalse(xor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun shouldCalculateMultiBit() {
		val xorGate = NonUnaryLogicGate(Xor, bitWidth = BitWidth.BW_2)
		xorGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(True, False)), signalHandler)
		xorGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(False, False)), signalHandler)

		val result = gateType.calculator.calculateMultiBit(xorGate)

		assertEquals(Word(listOf(True, False)), result)
	}
}
