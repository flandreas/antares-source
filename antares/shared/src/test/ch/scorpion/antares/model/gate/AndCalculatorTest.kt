package ch.scorpion.antares.model.gate

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DefinedWord
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [AndCalculator]. */
class AndCalculatorTest : AbstractGateCalculatorTest(AndCalculator()){

    companion object {
	    init {
		    AntaresTestRule.configure()
	    }
    }

	@Test
	fun shouldFulfillTruthTable() {
		CurrentUndefinedGateInputBehavior.value = UndefinedGateInputBehavior.ReadAs0

		assertTwoInput(False, False, False)
		assertTwoInput(False, True, False)
		assertTwoInput(False, Undefined, False)
		assertTwoInput(False, Error, Error)

		assertTwoInput(True, False, False)
		assertTwoInput(True, True, True)
		assertTwoInput(True, Undefined, False)
		assertTwoInput(True, Error, Error)

		assertTwoInput(Undefined, False, False)
		assertTwoInput(Undefined, True, False)
		assertTwoInput(Undefined, Undefined, False)
		assertTwoInput(Undefined, Error, Error)

		assertTwoInput(Error, False, Error)
		assertTwoInput(Error, True, Error)
		assertTwoInput(Error, Undefined, Error)
		assertTwoInput(Error, Error, Error)
	}

	@Test
	fun shouldCalculateSingleBit() {
		val andGate = AndGate(bitWidth = BitWidth.BW_1)
		andGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(False)), signalHandler)
		andGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(True)), signalHandler)

		val result = calculator.calculateSingleBit(andGate)

		assertEquals(DefinedWord.of(false), result)
	}

	@Test
	fun shouldCalculateMultiBit() {
		val andGate = AndGate(bitWidth = BitWidth.BW_2)
		andGate.getInput<DigitalSignal>(1).setIncomingSignal(Word(listOf(True, False)), signalHandler)
		andGate.getInput<DigitalSignal>(2).setIncomingSignal(Word(listOf(True, True)), signalHandler)

		val result = calculator.calculateMultiBit(andGate)

		assertEquals(Word(listOf(True, False)), result)
	}
}
