package io.antarescircuit.antares.model.gate

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Xnor
import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [XnorCalculator]. */
class XnorCalculatorTest : AbstractGateCalculatorTest(Xnor){

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertTwoInput(False, False, True)
		assertTwoInput(False, True, False)
		assertTwoInput(False, Undefined, True)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, False)
		assertTwoInput(True, True, True)
		assertTwoInput(True, Undefined, False)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, True)
		assertTwoInput(Undefined, True, False)
		assertTwoInput(Undefined, Undefined, True)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}

	@Test
	fun evenNumberOfInputShouldCalculateTrue() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		val xnor = NonUnaryLogicGate(Xnor, PortCount.THREE)
		xnor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)

		assertTrue(xnor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun oddNumberOfInputShouldCalculateFalse() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		val xnor = NonUnaryLogicGate(Xnor, PortCount.THREE)
		xnor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		assertFalse(xnor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun shouldCalculateMultiBit() {
		val xnorGate = NonUnaryLogicGate(Xnor, bitWidth = BitWidth.BW_2)
		xnorGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(True, False)), signalHandler)
		xnorGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(False, False)), signalHandler)

		val result = gateType.calculator.calculateMultiBit(xnorGate)

		assertEquals(Word(listOf(False, True)), result)
	}
}
