package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [XorCalculator]. */
class XorCalculatorTest : AbstractGateCalculatorTest(XorCalculator()){

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFulfillTruthTable() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Accept

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
	fun shouldCalculateWithErrorForUndefinedInput() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Error

		assertTwoInput(False, False, False)
		assertTwoInput(False, True, True)
		assertTwoInput(False, Undefined, Error)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, True)
		assertTwoInput(True, True, False)
		assertTwoInput(True, Undefined, Error)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, Error)
		assertTwoInput(Undefined, True, Error)
		assertTwoInput(Undefined, Undefined, Error)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}

	@Test
	fun oddNumberOfInputShouldCalculateTrue() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Accept

		val xor = XorGate(InputCount.THREE)
		xor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)

		assertTrue(xor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun evenNumberOfInputShouldCalculateFalse() {
		CurrentOpenGateInputBehaviour.value = OpenGateInputBehavior.Accept

		val xor = XorGate(InputCount.THREE)
		xor.getInput<DigitalSignal>(1).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(2).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		xor.getInput<DigitalSignal>(3).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)

		assertFalse(xor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}
}
