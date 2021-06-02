package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [XnorCalculator]. */
class XnorCalculatorTest : AbstractGateCalculatorTest(XnorCalculator()){

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldFulfillTruthTable() {
		assertTwoInput(False, False, True)
		assertTwoInput(False, True, False)
		assertTwoInput(False, Undefined, True)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, False)
		assertTwoInput(True, True, True)
		assertTwoInput(True, Undefined, True)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, True)
		assertTwoInput(Undefined, True, True)
		assertTwoInput(Undefined, Undefined, True)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}

	@Test
	fun evenNumberOfInputShouldCalculateTrue() {
		val xnor = XnorGate(InputCount.THREE)
		xnor.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(2).setIncomingSignal(Word.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(3).setIncomingSignal(Word.of(false), signalHandler)

		assertTrue(xnor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}

	@Test
	fun oddNumberOfInputShouldCalculateFalse() {
		val xnor = XnorGate(InputCount.THREE)
		xnor.getInput<DigitalSignal>(1).setIncomingSignal(Word.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(2).setIncomingSignal(Word.of(true), signalHandler)
		xnor.getInput<DigitalSignal>(3).setIncomingSignal(Word.of(true), signalHandler)

		assertFalse(xnor.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0).isSet)
	}
}
