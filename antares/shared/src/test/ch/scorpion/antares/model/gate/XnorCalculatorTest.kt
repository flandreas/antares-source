package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [XnorCalculator]. */
class XnorCalculatorTest : AbstractGateCalculatorTest(XnorCalculator()){

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

		val xnor = XnorGate(PortCount.THREE)
		xnor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)

		assertTrue(xnor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun oddNumberOfInputShouldCalculateFalse() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		val xnor = XnorGate(PortCount.THREE)
		xnor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		assertFalse(xnor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun shouldCalculateMultiBit() {
		val xnorGate = XnorGate(bitWidth = BitWidth.BW_2)
		xnorGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(True, False)), signalHandler)
		xnorGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(False, False)), signalHandler)

		val result = calculator.calculateMultiBit(xnorGate)

		assertEquals(Word(listOf(False, True)), result)
	}
}
